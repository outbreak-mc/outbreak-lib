package space.outbreak.lib.db.locale

import org.jetbrains.exposed.dao.id.IntIdTable

internal class CustomColorTagTable(name: String) : IntIdTable(name) {
    val namespace = varchar("namespace", 128).index().nullable()
    val tag = varchar("tag", 64).index()
    val hex = varchar("color", 9)
}