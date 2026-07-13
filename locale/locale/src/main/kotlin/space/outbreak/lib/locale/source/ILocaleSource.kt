package space.outbreak.lib.locale.source

import net.kyori.adventure.key.Key

interface ILocaleSource {
    /** Уникальный идентификатор источника */
    val key: Key

    fun init() {}
}