package space.outbreak.lib.v2.utils.paper

import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.FileInputStream
import java.util.*

fun Listener.registerThisListener(plugin: JavaPlugin) {
    plugin.server.pluginManager.registerEvents(this, plugin)
}

/**
 * @return [Audience], включающую всех игроков с
 * привилегией [permission] (если не `null`),
 * кроме [executor] (если не `null`) и консоль.
 * */
fun getAudienceForCommandLog(permission: String?, executor: Audience?): Audience {
    val players = Bukkit.getOnlinePlayers()
        .filter {
            (permission == null || it.hasPermission(permission))
                    && (executor == null || executor !is Player || it.uniqueId != executor.uniqueId)
        }.toTypedArray()

    return Audience.audience(*players, Bukkit.getConsoleSender())
}

/** @return имя основного мира, указанного в `server.properties` */
fun getLevelName(): String {
    val pr = Properties()
    pr.load(FileInputStream(File("server.properties")))
    return pr.getProperty("level-name")
}