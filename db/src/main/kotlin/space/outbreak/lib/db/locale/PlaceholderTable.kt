package space.outbreak.lib.db.locale

import org.jetbrains.exposed.dao.id.IntIdTable

internal class PlaceholderTable(name: String) : IntIdTable(name) {
    val namespace = varchar("namespace", 128).index().nullable()
    val lang = varchar("lang", 5).index().nullable()
    val placeholder = varchar("tag", 64).index()
    val value = text("value")
}