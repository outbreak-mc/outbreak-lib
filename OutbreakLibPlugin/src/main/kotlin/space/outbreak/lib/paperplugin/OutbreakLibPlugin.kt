package space.outbreak.lib.paperplugin

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import org.bukkit.plugin.java.JavaPlugin
import space.outbreak.lib.ConfigUtils
import space.outbreak.lib.db.connectToDB
import space.outbreak.lib.db.initDatabaseTables
import space.outbreak.lib.db.loadAllFromDB
import space.outbreak.lib.locale.KeyFormat
import space.outbreak.lib.locale.LocaleDataManager
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import kotlin.time.measureTime

class OutbreakLibPlugin : JavaPlugin() {
    private val configUtils = ConfigUtils(dataPath)
    private val command by lazy {
        LocaleDebugCommand(this)
    }
    private val localeDbProps = dataFolder.resolve("db.properties")
    private var loadTime: Long = 0L

    private fun prepareFiles() {
        if (!localeDbProps.exists()) {
            configUtils.extractAndGetResourceFile("_db.properties")
        }
    }

    var job: CompletableFuture<*>? = null

    fun printStats(): List<Component> {
        val nss = LocaleDataManager.namespaces
        var keys = 0
        var placeholders = 0
        var tags = 0

        for (ns in nss) {
            val data = LocaleDataManager.data(ns)
            placeholders += data.getGlobalPlaceholders().size
            tags += data.getCustomColorTags().size

            for (lang in data.languages) {
                keys += data.getKeys(lang, KeyFormat.ENUM_STYLE).size
                placeholders += data.getLangSpecificPlaceholders(lang).size
            }
        }

        val text = arrayOf(
            "OutbreakLib loaded locales in <yellow>${loadTime}</yellow> ms",
            "Namespaces: (<yellow>${nss.size}</yellow>) ${nss.joinToString(", ") { "<green>${it}</green>" }}",
            "Total: <yellow>${keys}</yellow> keys | <yellow>${placeholders}</yellow> placeholders | <yellow>${tags}</yellow> color tags"
        )

        return text.map { miniMessage().deserialize(it) }
    }

    override fun onLoad() {
        job = CompletableFuture.runAsync({
            reload()
        }, Executors.newCachedThreadPool())
    }

    fun reload() {
        prepareFiles()
        loadTime = measureTime {
            if (localeDbProps.exists()) {
                val db = connectToDB(localeDbProps)
                LocaleDataManager.initDatabaseTables(db)
                LocaleDataManager.loadAllFromDB(db, listOf("*"))
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
                "<#ffb3e2>DB completely loaded in background and did not took any start time!</#ffb3e2> <yellow>✨"
            else
                "<#ffb3e2>Yay! Only waited for db synchronously for <yellow>$lastedTime</yellow> ms!")

            componentLogger.info(miniMessage().deserialize(msg))
        }

        printStats().forEach { componentLogger.info(it) }
        command.register()
    }

    override fun onDisable() {
        command.unregister()
        job?.cancel(true)
    }
}