package space.outbreak.lib.v2.locale.source

import java.util.*

interface IPlaceholdersSource : ILocaleSource {
    fun getGlobalPlaceholders(serverName: String): MutableMap<String, MutableMap<String, String>>
    fun getPlaceholders(serverName: String): MutableMap<String, MutableMap<Locale, MutableMap<String, String>>>
}