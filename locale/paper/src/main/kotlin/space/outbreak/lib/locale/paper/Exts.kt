package space.outbreak.lib.locale.paper

import dev.jorel.commandapi.CommandAPIPaper
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import org.bukkit.inventory.meta.ItemMeta
import space.outbreak.lib.locale.IL
import space.outbreak.lib.locale.LPB
import space.outbreak.lib.locale.SealedLocaleBase

fun IL.cmdFail(vararg placeholders: LPB): Nothing {
    throw CommandAPIPaper.failWithAdventureComponent(this.tcomp(*placeholders))
}

fun SealedLocaleBase.cmdFail(): Nothing {
    throw CommandAPIPaper.failWithAdventureComponent(this.tcomp())
}

fun ItemMeta.miniMessageLore(stringLore: String) {
    this.lore(
        stringLore.replace("\r", "")
            .split(Regex("\n|<newline>"))
            .map { miniMessage().deserialize("<!i>${it}</!i>") })
}

fun Audience.sendMessage(msg: IL) {
    this.sendMessage(msg.tcomp())
}