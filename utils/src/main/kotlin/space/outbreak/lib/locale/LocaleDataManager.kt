package space.outbreak.lib.locale

object LocaleDataManager {
    private val dataMap = mutableMapOf<NamespaceKey, LocaleData>()

    val namespaces: Set<String>
        get() = dataMap.keys

    fun data(namespace: String): LocaleData {
        return dataMap.getOrPut(namespace) { LocaleData() }
    }

    fun clear() {
        for (d in dataMap.values)
            d.clear()
    }
}