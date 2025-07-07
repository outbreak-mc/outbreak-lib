package space.outbreak.lib.db.locale

import org.jetbrains.exposed.dao.id.IntIdTable

internal class LocaleTable(name: String = "locale") : IntIdTable(name) {
    /** Название плагина, которому принадлежит перевод */
    val namespace = varchar("namespace", 128).index()

    /** Название языка */
    val lang = varchar("lang", 5).index()

    /** Путь в формате как в yaml */
    val key = varchar("key", 256).index()

    /** Текст перевода */
    val value = text("value")
}