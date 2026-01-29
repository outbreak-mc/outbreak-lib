package space.outbreak.lib.v2.locale.db

import org.jetbrains.exposed.dao.id.IntIdTable

class LocaleTable(name: String = "locale") : IntIdTable(name) {
    /** Название плагина, которому принадлежит перевод */
    val namespace = varchar("namespace", 64).index()

    /** Название сервера, которому принадлежит перевод */
    val server = varchar("server", 64).nullable().index()

    /** Название языка */
    val lang = varchar("lang", 5).index()

    /** Путь в формате как в yaml */
    val key = varchar("key", 256).index()

    /** Текст перевода */
    val value = text("value")
}