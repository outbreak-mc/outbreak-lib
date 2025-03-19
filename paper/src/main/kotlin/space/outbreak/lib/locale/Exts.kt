package space.outbreak.lib.locale

import dev.jorel.commandapi.CommandAPIBukkit

fun ILocaleEnum.cmdFail(vararg placeholders: Pair<String, Any>): Nothing {
    throw CommandAPIBukkit.failWithAdventureComponent(this.comp(*placeholders))
}