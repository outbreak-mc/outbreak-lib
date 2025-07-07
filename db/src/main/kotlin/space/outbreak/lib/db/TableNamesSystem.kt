package space.outbreak.lib.db

import space.outbreak.lib.db.locale.CustomColorTagTable
import space.outbreak.lib.db.locale.LocaleTable
import space.outbreak.lib.db.locale.PlaceholderTable

/** Система имён для таблиц */
data class TableNamesSystem(
    val locale: String = "locale",
    val customColorTag: String = "custom_color_tag",
    val placeholder: String = "placeholder",
) {
    internal inner class Tables {
        val locale: LocaleTable = LocaleTable(this@TableNamesSystem.locale)
        val customColorTag: CustomColorTagTable = CustomColorTagTable(this@TableNamesSystem.customColorTag)
        val placeholder: PlaceholderTable = PlaceholderTable(this@TableNamesSystem.placeholder)
    }

    internal val tables = Tables()
}
