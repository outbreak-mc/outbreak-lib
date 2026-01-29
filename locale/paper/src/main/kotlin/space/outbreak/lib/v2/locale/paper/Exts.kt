package space.outbreak.lib.v2.locale.paper

import dev.jorel.commandapi.CommandAPIPaper
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import org.bukkit.inventory.meta.ItemMeta
import space.outbreak.lib.v2.locale.IL
import space.outbreak.lib.v2.locale.LocalePairBase
import space.outbreak.lib.v2.locale.SealedLocaleBase

fun IL.cmdFail(vararg placeholders: LocalePairBase<*>): Nothing {
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

//fun ItemMeta.lore(localeLore: IL, vararg replacing: LocalePairBase) {
//    this.lore(
//        localeLore.raw().replace("\r", "")
//            .split(Regex("\n|<newline>"))
//            .map { localeLore.data.formatter.process("<!i>${it}</!i>", null, *replacing) })
//}