package space.outbreak.lib.v2.locale.source.yaml

import net.kyori.adventure.key.Key
import org.yaml.snakeyaml.Yaml
import space.outbreak.lib.v2.locale.source.ICustomColorTagsSource
import java.io.File

class YamlColorTagsSource(
    override val key: Key,
    val file: File
) : ICustomColorTagsSource {
    override fun getCustomColorTags(): Map<String, String> {
        if (!file.exists()) {
            System.err.println("Unable to load custom color tags from source ${this::class.simpleName}: Directory $file does not exist")
            return mapOf()
        }

        val yaml = Yaml()
        val map: Map<String, String> = yaml.load(file.inputStream())
        return map
    }
}