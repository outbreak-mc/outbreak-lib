package space.outbreak.lib.paper

import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

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