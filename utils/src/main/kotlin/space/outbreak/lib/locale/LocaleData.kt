package space.outbreak.lib.locale

import net.kyori.adventure.text.minimessage.MiniMessage

object LocaleData {
    internal val data = _LocaleData()

    fun clear() {
        data.clear()
    }

    fun load(lang: String, config: Map<String, Any?>) {
        data.load(lang, config)
    }

    fun addLangSpecificStaticPlaceholders(lang: String, placeholders: Map<String, String>) {
        data.placeholders.getOrPut(lang) { mutableMapOf() }.putAll(placeholders)
    }

    fun addGlobalStaticPlaceholders(placeholders: Map<String, String>) {
        data.placeholdersGlobal.putAll(placeholders)
    }

    fun addCustomColorTags(tags: Map<String, String>) {
        data.customColorTags.putAll(tags)
        data.recalculateSerializer()
    }

    var defaultLang: String
        get() = data.defaultLang
        set(value) {
            data.defaultLang = value
        }

    val serializer: MiniMessage
        get() = data.serializer
}