package space.outbreak.lib.locale.source

import net.kyori.adventure.key.Key
import org.yaml.snakeyaml.Yaml
import space.outbreak.lib.locale.GlobalLocaleData.toYamlStyleKey
import java.io.File
import java.util.*

class YamlDirectoryLocaleSource(
    private val namespace: String,
    private val directory: File
) : ITranslationsSource {
    private fun compileMap(namespace: String, map: Map<String, Any?>): MutableMap<Key, String> {
        val out = mutableMapOf<Key, String>()
        map.forEach { (rawK, v) ->
            val k = rawK.toYamlStyleKey()
            if (v !is Map<*, *>) {
                out[Key.key(namespace, k)] = v.toString()
            } else {
                @Suppress("UNCHECKED_CAST")
                compileMap(namespace, v as Map<String, Any>).forEach { (innerK, innerV) ->
                    out[Key.key(namespace, k + "." + innerK.value())] = innerV
                }
            }
        }
        return out
    }

    private fun getLangByName(name: String): Locale? {
        val (name, _) = name.split(".", limit = 2)
        if (name.length != 5)
            return null
        val spl = name.split("_")
        if (spl.size != 2)
            return null
        val (lang, country) = spl
        return Locale.of(
            lang.lowercase(),
            country.uppercase()
        )
    }

    override fun getAllTranslations(serverName: String): Map<Locale, Map<Key, String>> {
        val yaml = Yaml()
        val result = mutableMapOf<Locale, Map<Key, String>>()

        directory.listFiles { it.name.endsWith(".yml") || it.name.endsWith(".yaml") }
            .mapNotNull { (getLangByName(it.name) ?: return@mapNotNull null) to it }
            .forEach { (lang, f) ->
                val map: Map<String, Any> = yaml.load(f.inputStream())
                result[lang] = compileMap(namespace, map)
            }

        return result
    }
}