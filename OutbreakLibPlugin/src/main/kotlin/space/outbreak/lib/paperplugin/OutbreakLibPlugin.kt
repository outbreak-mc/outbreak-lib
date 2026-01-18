package space.outbreak.lib.paperplugin

import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import space.outbreak.lib.locale.GlobalLocaleData
import space.outbreak.lib.locale.LocaleData
import space.outbreak.lib.locale.cache.MsgCache
import space.outbreak.lib.locale.db.LocaleDb
import space.outbreak.lib.utils.ConfigUtils
import space.outbreak.lib.utils.db.connectToDB
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import kotlin.io.path.absolute
import kotlin.io.path.name
import kotlin.time.measureTime

class OutbreakLibPlugin : JavaPlugin() {
    private val configUtils = ConfigUtils(dataPath, this.javaClass.classLoader)
    private val command by lazy { LocaleDebugCommand(this, GlobalLocaleData) }
    private val localeDbProps = dataFolder.resolve("db.properties")
    private var loadTime: Long = 0L

    private fun prepareFiles() {
        if (!localeDbProps.exists()) {
            configUtils.extractAndGetResourceFile("_db.properties")
        }
    }

    private var job: CompletableFuture<*>? = null

    private fun getServerName(): String {
        val nameInConfig = config.getString("server-name")
        return if (nameInConfig.isNullOrBlank())
            dataPath.absolute().parent.parent.name
        else
            nameInConfig
    }

    fun printStats(ld: LocaleData): L.LOADED__STATS {
        val nss = ld.namespaces
        var keys = 0
        var tags = 0

        for (ns in nss) {
            tags += ld.getCustomColorTags().size

            for (lang in ld.languages) {
                keys += ld.getKeys(lang).size
            }
        }

        return L.LOADED__STATS(
            `load-time` = loadTime,
            ns = nss,
            `total-keys` = keys,
            `total-color-tags` = tags,
//            `total-placeholders` = placeholders
        )
    }

    override fun onLoad() {
        saveDefaultConfig()
        job = CompletableFuture.runAsync({
            reload()
        }, Executors.newCachedThreadPool())
    }

    fun reload() {
        prepareFiles()
        MsgCache.clear()

        loadTime = measureTime {
            configUtils.loadLocalesFolder("outbreaklib", GlobalLocaleData)

            if (localeDbProps.exists()) {
                val localeDb = LocaleDb(connectToDB(localeDbProps))
                localeDb.initDatabaseTables()
                localeDb.loadAllFromDB(server = getServerName())

            } else {
                configUtils.extractAndGetResourceFile("_db.properties")
                logger.severe("Locale database not loaded! Configure \"_db.properties\" correctly and rename it to \"db.properties\"")
            }
        }.inWholeMilliseconds
    }

    override fun onEnable() {
        val lastedTime = measureTime {
            job?.join()
        }.inWholeMilliseconds
        if (lastedTime < loadTime) {
            val msg = (if (lastedTime == 0L)
                "<#ffb3e2>DB completely loaded in background and did not take any start time!</#ffb3e2> <yellow>✨"
            else
                "<#ffb3e2>Yay! Only waited for db synchronously for <yellow>$lastedTime</yellow> ms!")

            componentLogger.info(miniMessage().deserialize(msg))
        }

        printStats(GlobalLocaleData).send(Bukkit.getConsoleSender())
        command.register()
    }

    override fun onDisable() {
        command.unregister()
        job?.cancel(true)
        GlobalTranslator.translator().removeSource(GlobalLocaleData.translator)
    }
}