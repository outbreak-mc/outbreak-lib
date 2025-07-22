package space.outbreak.lib.paper

import net.kyori.adventure.audience.Audience
import org.bukkit.entity.Player
import space.outbreak.lib.locale.ILocaleEnum
import space.outbreak.lib.locale.pair.LocalePairBase

interface ILocaleEnumPaper : ILocaleEnum {
    override fun send(audience: Audience, vararg replacing: LocalePairBase) {
        audience.sendMessage(comp(if (audience is Player) audience.locale().language else null, *replacing))
    }

    override fun sendActionBar(audience: Audience, vararg replacing: LocalePairBase) {
        audience.sendActionBar(comp(if (audience is Player) audience.locale().language else null, *replacing))
    }
}