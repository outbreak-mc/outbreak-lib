package space.outbreak.lib.locale

import net.kyori.adventure.text.minimessage.MiniMessage
import org.yaml.snakeyaml.Yaml
import java.io.File

object LocaleData {
    internal val data = _LocaleData()
    private val yaml = Yaml()

    fun getRaw(lang: LangKey, key: TranslationKey): String? {
        val k = if (key.contains(".") || key.contains("-")) {
            key.uppercase()
                .replace(".", "__")
                .replace("-", "_")
        } else
            key
        return data.compiledTree[lang]?.get(k)
    }

    fun getLanguages(): Set<String> {
        return data.languages
    }

    fun getKeys(lang: LangKey, yamlFormat: Boolean): Collection<TranslationKey> {
        val keys = (data.compiledTree[lang] ?: mapOf()).keys
        return if (yamlFormat) {
            keys.map {
                it.lowercase()
                    .replace("__", ".")
                    .replace("_", "-")
            }
        } else {
            keys
        }
    }

    fun clear() {
        data.clear()
    }

    fun load(lang: String, config: Map<String, Any?>) {
        data.load(lang, config)
    }

    fun load(lang: String, file: File) {
        data.load(lang, yaml.load(file.inputStream()))
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