package space.outbreak.lib.paperplugin

import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import space.outbreak.lib.api.locale.GlobalLocaleDataManager
import space.outbreak.lib.utils.ConfigUtils
import space.outbreak.lib.utils.db.connectToDB
import space.outbreak.lib.utils.locale.KeyStyle
import space.outbreak.lib.utils.locale.db.initDatabaseTables
import space.outbreak.lib.utils.locale.db.loadAllFromDB
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import kotlin.io.path.absolute
import kotlin.io.path.name
import kotlin.time.measureTime

class OutbreakLibPlugin : JavaPlugin() {
    private val configUtils = ConfigUtils(dataPath, this.javaClass.classLoader)
    private val command by lazy { LocaleDebugCommand(this) }
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

    fun printStats(): L.LOADED__STATS {
        val nss = GlobalLocaleDataManager.namespaces
        var keys = 0
        var placeholders = 0
        var tags = 0

        for (ns in nss) {
            val data = GlobalLocaleDataManager.data(ns)
            placeholders += data.getGlobalPlaceholders().size
            tags += data.getCustomColorTags().size

            for (lang in data.languages) {
                keys += data.getKeys(lang, KeyStyle.ENUM_KEY).size
                placeholders += data.getLangSpecificPlaceholders(lang).size
            }
        }

        return L.LOADED__STATS(
            `load-time` = loadTime,
            ns = nss,
            `total-keys` = keys,
            `total-color-tags` = tags,
            `total-placeholders` = placeholders,
            nsFormat = L.LOADED__NS_FORMAT().raw(null)
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
        loadTime = measureTime {
            if (localeDbProps.exists()) {
                val db = connectToDB(localeDbProps)
                GlobalLocaleDataManager.initDatabaseTables(db)
                GlobalLocaleDataManager.loadAllFromDB(db, listOf("*"), server = getServerName())
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

        printStats().send(Bukkit.getConsoleSender())
        command.register()
    }

    override fun onDisable() {
        command.unregister()
        job?.cancel(true)
    }
}