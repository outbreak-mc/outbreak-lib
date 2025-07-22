package space.outbreak.lib.locale

object LocaleDataManager {
    private val globalNsLocaleData = LocaleData()

    private val dataMap = mutableMapOf<NamespaceKey, LocaleData>()

    val namespaces: Set<String>
        get() = dataMap.keys

    fun data(namespace: String): LocaleData {
        return dataMap.getOrPut(namespace) { globalNsLocaleData.copy() }
    }

    fun clear() {
        for (d in dataMap.values)
            d.clear()
    }

    fun load(namespace: String, lang: String, dictionary: MutableMap<String, String>) {
        if (namespace == "*") {
            globalNsLocaleData.load(lang, dictionary)
            for (d in dataMap.values)
                d.load(lang, dictionary)
        } else
            data(namespace).load(lang, dictionary)
    }

    fun addCustomColorTags(namespace: String, tags: MutableMap<String, String>) {
        if (namespace == "*") {
            globalNsLocaleData.addCustomColorTags(tags)
            for (d in dataMap.values)
                d.addCustomColorTags(tags)
        } else
            data(namespace).addCustomColorTags(tags)
    }

    fun addPlaceholders(namespace: String, lang: String?, placeholders: MutableMap<String, String>) {
        if (namespace == "*") {
            globalNsLocaleData.addPlaceholders(lang, placeholders)
            for (d in dataMap.values)
                d.addPlaceholders(lang, placeholders)
        } else
            data(namespace).addPlaceholders(lang, placeholders)
    }
}