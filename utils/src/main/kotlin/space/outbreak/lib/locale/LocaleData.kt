package space.outbreak.lib.locale

import com.fasterxml.jackson.core.type.TypeReference
import net.kyori.adventure.text.minimessage.MiniMessage
import space.outbreak.lib.createYamlMapper
import java.io.File

object LocaleData {
    internal val data = _LocaleData()
    private val yamlMapper = createYamlMapper()

    fun clear() {
        data.clear()
    }

    fun load(lang: String, config: Map<String, Any?>) {
        data.load(lang, config)
    }

    fun load(lang: String, file: File) {
        val f = this.yamlMapper.readValue(file, object : TypeReference<Map<String, Any>>() {})
        data.load(lang, f)
    }

    fun removeLang(lang: String) {
        data.removeLang(lang)
    }

    fun addLangSpecificStaticPlaceholders(lang: String, placeholders: Map<String, String>) {
        data.placeholdersLangSpecific.getOrPut(lang) { mutableMapOf() }.putAll(placeholders)
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