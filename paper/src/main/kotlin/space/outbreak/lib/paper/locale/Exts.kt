package space.outbreak.lib.paper.locale

import dev.jorel.commandapi.CommandAPIBukkit
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import org.bukkit.inventory.meta.ItemMeta
import space.outbreak.lib.locale.ILocaleEnum
import space.outbreak.lib.locale.SealedLocaleBase
import space.outbreak.lib.locale.pair.LocalePairBase

fun ILocaleEnum.cmdFail(vararg placeholders: LocalePairBase): Nothing {
    throw CommandAPIBukkit.failWithAdventureComponent(this.comp(*placeholders))
}

fun SealedLocaleBase.cmdFail(): Nothing {
    throw CommandAPIBukkit.failWithAdventureComponent(this.comp())
}

fun ItemMeta.miniMessageLore(stringLore: String) {
    this.lore(stringLore.replace("\r", "")
        .split(Regex("\n|<newline>"))
        .map { miniMessage().deserialize("<!i>${it}</!i>") })
}

fun ItemMeta.lore(localeLore: ILocaleEnum, vararg replacing: LocalePairBase) {
    this.lore(localeLore.raw().replace("\r", "")
        .split(Regex("\n|<newline>"))
        .map { localeLore.data.formatter.process("<!i>${it}</!i>", null, *replacing) })
}