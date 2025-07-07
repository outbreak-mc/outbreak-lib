package space.outbreak.lib.paper

import net.kyori.adventure.audience.Audience
import org.bukkit.entity.Player
import space.outbreak.lib.locale.ILocaleEnum

interface ILocaleEnumPaper : ILocaleEnum {
    override fun send(audience: Audience, vararg replacing: Pair<String, Any>) {
        audience.sendMessage(comp(if (audience is Player) audience.locale().language else null, *replacing))
    }

    override fun sendActionBar(audience: Audience, vararg replacing: Pair<String, Any>) {
        audience.sendActionBar(comp(if (audience is Player) audience.locale().language else null, *replacing))
    }
}