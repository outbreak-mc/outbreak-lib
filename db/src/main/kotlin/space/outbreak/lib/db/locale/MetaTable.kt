package space.outbreak.lib.db.locale

import org.jetbrains.exposed.dao.id.IntIdTable

internal class MetaTable(name: String = "locale_tables_meta") : IntIdTable(name) {
    val key = varchar("key", 32).index()
    val value = integer("data")
}