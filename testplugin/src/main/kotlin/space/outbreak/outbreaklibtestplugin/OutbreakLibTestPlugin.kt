package space.outbreak.outbreaklibtestplugin

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import space.outbreak.lib.ConfigUtils
import space.outbreak.lib.locale.LocaleData

class OutbreakLibTestPlugin : JavaPlugin() {
    private val configUtils = ConfigUtils(this.dataFolder.toPath())

    override fun onEnable() {
        reload()
        L.IT_WORKS.send(Bukkit.getConsoleSender())
    }

    fun reload() {
        configUtils.loadLocales(LocaleData)
    }
}