package space.outbreak.lib.v2.locale.source.yaml

import net.kyori.adventure.key.Key
import org.yaml.snakeyaml.Yaml
import space.outbreak.lib.v2.locale.GlobalLocaleData.toYamlStyleKey
import space.outbreak.lib.v2.locale.source.ITranslationsSource
import java.io.File
import java.util.*

class SingleYamlFileTranslationsSource(
    private val lang: Locale,
    private val namespace: String,
    private val file: File
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

    override fun getAllTranslations(serverName: String): Map<Locale, Map<Key, String>> {
        if (!file.exists()) {
            System.err.println("Unable to load locale from source ${this::class.simpleName}: File $file does not exist")
            return mapOf()
        }

        val map: Map<String, Any> = Yaml().load(file.inputStream())
        return mapOf(lang to compileMap(namespace, map))
    }
}