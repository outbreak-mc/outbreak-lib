package space.outbreak.lib.v2.paperplugin

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import space.outbreak.lib.v2.locale.GlobalLocaleData
import space.outbreak.lib.v2.locale.cache.MsgCache
import space.outbreak.lib.v2.locale.db.CURRENT_LOCALE_DB_SCHEMA_VERSION
import space.outbreak.lib.v2.locale.db.LocaleTableNamesSystem
import space.outbreak.lib.v2.locale.db.SQLLocaleSource
import space.outbreak.lib.v2.locale.source.YamlDirectoryLocaleSource
import space.outbreak.lib.v2.utils.db.connectToDB
import space.outbreak.lib.v2.utils.resapi.Res
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import kotlin.io.path.absolute
import kotlin.io.path.name
import kotlin.time.measureTime

class OutbreakLibPlugin : JavaPlugin() {
    val ld = GlobalLocaleData
    private val command by lazy { LocaleDebugCommand(this, ld) }
    private val localeDbProps = dataFolder.resolve("db.properties")
    private val res = Res(this.javaClass.classLoader)
    private val yamlConfigsLocaleSource = YamlDirectoryLocaleSource(
        "outbreaklib", dataFolder.resolve("messages").resolve("locales")
    )

    class LoadStats(
        val namespaces: Set<String> = setOf(),
        val keys: Int = -1,
        val tags: Int = -1,
        var loadTime: Long = -1L
    )

    private var loadStats = LoadStats()

    private fun prepareFiles() {
        res.extract("messages", dataFolder)
    }

    private var job: CompletableFuture<*>? = null

    private fun getServerName(): String {
        val nameInConfig = config.getString("server-name")
        return if (nameInConfig.isNullOrBlank())
            dataPath.absolute().parent.parent.name
        else
            nameInConfig
    }

    fun printStats(): L.LOADED__STATS {
        return L.LOADED__STATS(
            `load-time` = loadStats.loadTime,
            ns = loadStats.namespaces,
            `total-keys` = loadStats.keys,
            `total-color-tags` = loadStats.tags,
        )
    }

    override fun onLoad() {
        saveDefaultConfig()
        job = CompletableFuture.runAsync({
            reload()
        }, Executors.newCachedThreadPool())
    }

    fun benchmark(chunksCount: Int = 10, chunksSize: Int = 100, audience: Audience?): L.BENCHMARK {
        var notCachedTime = 0L
        var cachedTime = 0L

        val cachedComp = printStats().tcomp()
        val directComp = printStats().tcomp(ray = -1)

        val locale = Locale.of("en", "US")

        for (c in 0..chunksCount) {
            if (c % 2 == 0) {
                notCachedTime += measureTime {
                    for (i in 0..chunksSize) {
                        if (audience == null) {
                            GlobalTranslator.render(directComp, locale)
                        } else {
                            audience.sendMessage(directComp)
                        }
                    }
                }.inWholeMicroseconds
            } else {
                cachedTime += measureTime {
                    for (i in 0..chunksSize) {
                        if (audience == null) {
                            GlobalTranslator.render(cachedComp, locale)
                        } else {
                            audience.sendMessage(cachedComp)
                        }
                    }
                }.inWholeMicroseconds
            }
        }

        return L.BENCHMARK(
            messages = chunksCount * chunksSize,
            `cached-time` = cachedTime / 1000L,
            `direct-time` = notCachedTime / 1000L,
            `batch-size` = chunksSize,
            batches = chunksCount
        )
    }

    fun reload(): LoadStats {
        prepareFiles()
        MsgCache.clear()
        val server = getServerName()

        ld.serverName = server

        val loadTime = measureTime {
            ld.clearSources()
            ld.clearData()
            ld.addSource(yamlConfigsLocaleSource)

            if (localeDbProps.exists()) {
                val sqlSource = SQLLocaleSource(
                    server = server,
                    namespaces = listOf("*"),
                    db = connectToDB(localeDbProps),
                    tables = LocaleTableNamesSystem("outbreaklib"),
                    logger = slF4JLogger
                )
                sqlSource.checkAndInitDatabaseTables(
                    CURRENT_LOCALE_DB_SCHEMA_VERSION,
                    config.getBoolean("debug.migrate-if-unstable")
                )
                ld.addSource(sqlSource)
                ld.load()
            } else {
                res.extract("_db.properties", dataFolder)
                logger.severe("Unable to load locale from database! Configure \"_db.properties\" correctly and rename it to \"db.properties\"")
            }
        }.inWholeMilliseconds

        var keys = 0
        for (lang in ld.languages)
            keys += ld.getKeys(lang).size

        val stats = LoadStats(
            namespaces = ld.namespaces,
            tags = ld.getCustomColorTags().size,
            keys = keys,
            loadTime = loadTime
        )
        loadStats = stats
        return stats
    }

    override fun onEnable() {
        val lastedTime = measureTime {
            job?.join()
        }.inWholeMilliseconds

        if (loadStats.loadTime < 0)
            throw IllegalStateException("Load time is ${loadStats.loadTime} < 0. Something's wrong with the locales loading process.")

        if (lastedTime < loadStats.loadTime) {
            val msg = (if (lastedTime == 0L)
                "<#ffb3e2>DB completely loaded in background and did not take any start time!</#ffb3e2> <yellow>✨"
            else
                "<#ffb3e2>Yay! Only waited for db synchronously for <yellow>$lastedTime</yellow> ms!")

            componentLogger.info(miniMessage().deserialize(msg))
        }

        printStats().send(Bukkit.getConsoleSender())
        command.register()
    }

    override fun onDisable() {
        command.unregister()
        job?.cancel(true)
        GlobalTranslator.translator().removeSource(ld.translator)
    }
}