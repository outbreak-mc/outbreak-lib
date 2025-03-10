package space.outbreak.lib

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

class ConfigProvider<T>(
    private val path: String,
    private val dirPath: Path,
    private val type: Class<T>,
    private val mapper: YAMLMapper,
) {
    private var _conf: T? = null
    private val outPath = dirPath.resolve(path)

    val config: T
        get() {
            if (_conf == null) {
                _conf = load()
            }
            return _conf!!
        }

    @Suppress("unused")
    fun load(): T {
        if (!outPath.exists()) {
            val inputStream = object {}.javaClass.classLoader.getResourceAsStream(path)
                ?: throw FileNotFoundException("Resource not found: ${path}")
            Files.copy(inputStream, outPath)
        }

        return mapper.readValue(outPath.toFile(), type)
    }

    @Suppress("unused")
    fun save(data: T) {
        mapper.writeValue(outPath.toFile(), data)
    }
}