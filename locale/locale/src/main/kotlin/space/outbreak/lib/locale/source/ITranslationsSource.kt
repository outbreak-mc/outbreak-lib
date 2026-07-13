package space.outbreak.lib.locale.source

import net.kyori.adventure.key.Key
import java.util.*

interface ITranslationsSource : ILocaleSource {
    fun getAllTranslations(serverName: String): Map<Locale, Map<Key, String>>
}