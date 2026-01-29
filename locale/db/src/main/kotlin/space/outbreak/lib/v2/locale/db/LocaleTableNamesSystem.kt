package space.outbreak.lib.v2.locale.db

import space.outbreak.lib.v2.utils.db.DbVersionMetaTable

/** Система имён для таблиц */
data class LocaleTableNamesSystem(
    val prefix: String,
    val locale: String = "locale",
    val customColorTag: String = "custom_color_tag",
    val placeholder: String = "placeholder",
    val meta: String = "locale_tables_meta",
) {
    internal inner class Tables {
        val locale: LocaleTable = LocaleTable("${prefix}_${this@LocaleTableNamesSystem.locale}")
        val customColorTag: CustomColorTagTable =
            CustomColorTagTable("${prefix}_${this@LocaleTableNamesSystem.customColorTag}")
        val placeholder: PlaceholderTable = PlaceholderTable("${prefix}_${this@LocaleTableNamesSystem.placeholder}")
        val meta: DbVersionMetaTable = DbVersionMetaTable("${prefix}_${this@LocaleTableNamesSystem.meta}")
    }

    internal val tables = Tables()
}
