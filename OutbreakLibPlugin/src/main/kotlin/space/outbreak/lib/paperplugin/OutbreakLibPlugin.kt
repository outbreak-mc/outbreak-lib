package space.outbreak.lib.paperplugin

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.Database
import space.outbreak.lib.locale.GlobalLocaleData
import space.outbreak.lib.locale.db.LocaleTableNamesSystem
import space.outbreak.lib.locale.db.SQLLocaleSource
import space.outbreak.lib.locale.ofExactLocale
import space.outbreak.lib.locale.source.ILocaleSource
import space.outbreak.lib.locale.source.yaml.SingleYamlFileTranslationsSource
import space.outbreak.lib.locale.source.yaml.YamlColorTagsSource
import space.outbreak.lib.locale.source.yaml.YamlDirectoryLocaleSource
import space.outbreak.lib.utils.db.connectToDB
import space.outbreak.lib.utils.resapi.Res
import java.io.File
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import kotlin.io.path.absolute
import kotlin.io.path.name
import kotlin.time.measureTime

class OutbreakLibPlugin : JavaPlugin() {
    val ld = GlobalLocaleData
    private val command by lazy { OutbreakLibCommand(this, ld) }
    private val res = Res(this.javaClass.classLoader)

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

    fun printStats(): L.LOADED.STATS {
        return L.LOADED.STATS(
            `load-time` = loadStats.loadTime,
            ns = loadStats.namespaces,
            `total-keys` = loadStats.keys,
            `total-color-tags` = loadStats.tags,
        )
    }

    internal fun readSourcesFolder(sourcesFolder: File) {
        for (s in _sourcesFromFolder)
            ld.removeSource(s)
        _sourcesFromFolder.clear()

        fun loadFile(namespace: String, file: File): ILocaleSource? {
            val name = file.nameWithoutExtension
            return when (file.extension) {
                "properties" -> SQLLocaleSource(
                    key = Key.key(namespace, name),
                    server = getServerName(),
                    namespaces = listOf("*"),
                    db = Database.connect(connectToDB(file)),
                    tables = LocaleTableNamesSystem("outbreaklib"),
                    logger = slF4JLogger,
                    migrateIfUnstable = config.getBoolean("debug.migrate-if-unstable")
                )

                "yml", "yaml" -> {
                    if (file.nameWithoutExtension == "color-tags") {
                        YamlColorTagsSource(Key.key(namespace, name), file)
                    } else {
                        SingleYamlFileTranslationsSource(
                            Key.key(namespace, name.lowercase()),
                            ofExactLocale(file.nameWithoutExtension), file
                        )
                    }
                }

                else -> null
            }
        }

        fun loadDir(dir: File): List<ILocaleSource> {
            val namespace = dir.name
            val out = mutableListOf<ILocaleSource>()

            dir.listFiles().forEach { f ->
                (if (f.isDirectory) {
                    YamlDirectoryLocaleSource(Key.key(namespace, f.nameWithoutExtension), f)
                } else loadFile(namespace, f))
                    ?.also { source ->
                        out.add(source)
                        componentLogger.info("Loading $f as source ${source.key}")
                    }
            }
            return out
        }

        val out = mutableListOf<ILocaleSource>()
        sourcesFolder.listFiles().forEach { entry ->
            if (entry.isDirectory)
                out.addAll(loadDir(entry))
            else
                loadFile("__global__", entry)?.also(out::add)
        }

        for (s in out) {
            _sourcesFromFolder.add(s.key)
            ld.addSource(s)
        }
    }

    override fun onLoad() {
        saveDefaultConfig()
        job = CompletableFuture.runAsync({
            reload()
        }, Executors.newCachedThreadPool())
    }

    internal fun benchmark(chunksCount: Int = 10, chunksSize: Int = 100, audience: Audience?): L.BENCHMARK {
        var notCachedTime = 0L
        var cachedTime = 0L

        val cachedComp = printStats().tcomp()
        val directComp = printStats().tcomp()

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

    internal fun recalculateStats(loadTime: Long?): LoadStats {
        var keys = 0
        for (lang in ld.languages)
            keys += ld.getKeys(lang).size

        val stats = LoadStats(
            namespaces = ld.namespaces,
            tags = ld.getColorTags().size,
            keys = keys,
            loadTime = loadTime ?: loadStats.loadTime
        )

        return stats
    }

    private val _sourcesFromFolder = mutableListOf<Key>()

    internal fun reload(): LoadStats {
        prepareFiles()
        val server = getServerName()

        ld.serverName = server

        val loadTime = measureTime {
            ld.clearData()
            readSourcesFolder(dataFolder.resolve("sources"))
            ld.loadAll()
        }.inWholeMilliseconds

        return recalculateStats(loadTime).also { loadStats = it }
    }

    override fun onEnable() {
        val lastedTime = measureTime {
            job?.join()
        }.inWholeMilliseconds

        if (loadStats.loadTime < 0)
            throw IllegalStateException(
                "Load time is ${loadStats.loadTime} < 0. Something's wrong " +
                        "with the locales loading process."
            )

        if (lastedTime < loadStats.loadTime) {
            val msg = (if (lastedTime == 0L)
                "<#ffb3e2>Yay! Sources completely loaded in background and did not take any start time!</#ffb3e2> <yellow>✨"
            else
                "<#ffb3e2>Only waited for db synchronously for <yellow>$lastedTime</yellow> ms!")

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