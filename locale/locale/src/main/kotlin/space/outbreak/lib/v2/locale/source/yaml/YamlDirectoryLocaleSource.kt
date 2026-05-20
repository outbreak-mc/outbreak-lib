package space.outbreak.lib.v2.locale.source.yaml

import net.kyori.adventure.key.Key
import org.yaml.snakeyaml.Yaml
import space.outbreak.lib.v2.locale.ofExactLocaleOrNull
import space.outbreak.lib.v2.locale.source.ITranslationsSource
import space.outbreak.lib.v2.locale.toYamlStyleKey
import java.io.File
import java.util.*

open class YamlDirectoryLocaleSource(
    override val key: Key,
    private val directory: File
) : ITranslationsSource {
    protected fun compileMap(namespace: String, map: Map<String, Any?>): MutableMap<Key, String> {
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

    override fun getAllTranslations(serverName: String): Map<Locale, Map<Key, String>> {
        if (!directory.exists()) {
            System.err.println("Unable to load locale from source ${this::class.simpleName}: Directory $directory does not exist")
            return mapOf()
        }

        val yaml = Yaml()
        val result = mutableMapOf<Locale, Map<Key, String>>()

        directory.listFiles { it.name.endsWith(".yml") || it.name.endsWith(".yaml") }
            .mapNotNull { (ofExactLocaleOrNull(it.nameWithoutExtension) ?: return@mapNotNull null) to it }
            .forEach { (lang, f) ->
                val map: Map<String, Any> = yaml.load(f.inputStream())
                result[lang] = compileMap(key.namespace(), map)
            }

        return result
    }
}