package space.outbreak.lib.locale

import dev.jorel.commandapi.CommandAPIBukkit
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import org.bukkit.inventory.meta.ItemMeta

fun ILocaleEnum.cmdFail(vararg placeholders: Pair<String, Any>): Nothing {
    throw CommandAPIBukkit.failWithAdventureComponent(this.comp(*placeholders))
}

fun ItemMeta.miniMessageLore(stringLore: String) {
    this.lore(stringLore.replace("\r", "")
        .split(Regex("\n|<newline>"))
        .map { miniMessage().deserialize("<!i>${it}</!i>") })
}

fun ItemMeta.lore(localeLore: ILocaleEnum, vararg replacing: Pair<String, Any>) {
    this.lore(localeLore.raw(*replacing).replace("\r", "")
        .split(Regex("\n|<newline>"))
        .map { miniMessage().deserialize("<!i>${it}</!i>") })
}