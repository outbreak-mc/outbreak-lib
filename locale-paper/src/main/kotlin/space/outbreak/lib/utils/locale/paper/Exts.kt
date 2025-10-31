package space.outbreak.lib.utils.locale.paper

import dev.jorel.commandapi.CommandAPIBukkit
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import org.bukkit.inventory.meta.ItemMeta
import space.outbreak.lib.utils.locale.IL
import space.outbreak.lib.utils.locale.SealedLocaleBase
import space.outbreak.lib.utils.locale.pair.LocalePairBase

fun IL.cmdFail(vararg placeholders: LocalePairBase): Nothing {
    throw CommandAPIBukkit.failWithAdventureComponent(this.comp(*placeholders))
}

fun SealedLocaleBase.cmdFail(): Nothing {
    throw CommandAPIBukkit.failWithAdventureComponent(this.comp())
}

fun ItemMeta.miniMessageLore(stringLore: String) {
    this.lore(
        stringLore.replace("\r", "")
            .split(Regex("\n|<newline>"))
            .map { miniMessage().deserialize("<!i>${it}</!i>") })
}

fun ItemMeta.lore(localeLore: IL, vararg replacing: LocalePairBase) {
    this.lore(
        localeLore.raw().replace("\r", "")
            .split(Regex("\n|<newline>"))
            .map { localeLore.data.formatter.process("<!i>${it}</!i>", null, *replacing) })
}