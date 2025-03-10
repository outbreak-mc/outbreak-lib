package space.outbreak.lib

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import space.outbreak.lib.locale.ILocaleEnum
import space.outbreak.lib.locale.LocaleData
import space.outbreak.lib.locale.PlaceholdersConfig
import java.io.File
import java.io.FileNotFoundException
import java.net.URI
import java.nio.file.*
import java.util.stream.Stream
import kotlin.io.path.exists

class ConfigUtils(
    private val dataDir: Path,
) {
    private val yamlMapper: ObjectMapper = YAMLMapper.builder()
        .configure(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS, true)
        .build()
        .registerModule(
            KotlinModule.Builder()
                .withReflectionCacheSize(512)
                .configure(KotlinFeature.NullToEmptyCollection, false)
                .configure(KotlinFeature.NullToEmptyMap, false)
                .configure(KotlinFeature.NullIsSameAsDefault, false)
                .configure(KotlinFeature.SingletonSupport, false)
                .configure(KotlinFeature.StrictNullChecks, false)
                .build()
        )
    // .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    init {
        val module = SimpleModule()
        yamlMapper.registerModule(module)
    }

    /**
     * Находит в ресурсах файл [resourcePath], распаковывает его в папку
     * плагина, читает его как yaml и парсит в объект типа [type]
     * */
    fun <T> readConfig(resourcePath: URI, type: Class<T>): T {
        return yamlMapper.readValue(getResourceFile(resourcePath), type)
    }

    private fun URI.toResourcePathString(): String {
        return if (path.startsWith("/")) path.substring(1)
        else path
    }

    private fun Path.toResourcePathString(): String {
        val pstr = toUri().path
        return if (pstr.startsWith("/")) pstr.substring(1)
        else pstr
    }

    fun writeConfig(resourcePath: URI, data: Any) {
        yamlMapper.writeValue(getResourceFile(resourcePath), data)
    }

    fun getResourceFile(path: URI): File {
        val outputFile = dataDir.resolve(path.path)
        if (!outputFile.exists()) {
            val inputStream = object {}.javaClass.classLoader.getResourceAsStream(path.path)
                ?: throw FileNotFoundException("Resource not found: ${path}")
            Files.copy(inputStream, outputFile)
        }

        return outputFile.toFile()
    }

    fun getResourceFiles(path: URI): Stream<Path> {
        val uri = object {}.javaClass.getResource(path.path)!!.toURI()
        val dirPath = try {
            Paths.get(uri)
        } catch (e: FileSystemNotFoundException) {
            // If this is thrown, then it means that we are running the JAR directly (example: not from an IDE)
            val env = mutableMapOf<String, String>()
            FileSystems.newFileSystem(uri, env).getPath(path.path)
        }

        return Files.list(dirPath)
    }

    private fun readLocales(
        path: URI,
        func: (String, Map<String, Any>) -> Unit,
    ): List<String> {
        if (path.isAbsolute)
            throw IllegalArgumentException("Path must not be absolute")
        val out = mutableListOf<String>()
        // Сначала распаковываем файлы, которые только в ресурсах
        val resPath = URI("/messages").resolve(path)
        getResourceFiles(resPath).forEach { getResourceFile(resPath.resolve(it.toUri())) }
        // Затем читаем с диска
        dataDir.resolve(path.path).toFile().listFiles()?.filter { it.name.endsWith(".yml") }?.forEach {
            val (name, _) = it.name.split(".", limit = 2)
            func(name, yamlMapper.readValue(it, object : TypeReference<Map<String, Any>>() {}))
            out.add(name)
        }
        return out
    }

    /**
     * Загружает файлы локализации из папки плагина/messages.
     * Также дораспаковывает недостающие файлы локализаций из ресурсов.
     * */
    fun loadLocales(ld: LocaleData): List<String> {
        val path = URI("/messages")
        ld.clear()

        val locales = readLocales(path.resolve("locales")) { lang, data ->
            ld.load(lang, data)
        }

        if (locales.isEmpty())
            throw FileNotFoundException("No locales found neither in resources nor in plugin folder${path}!")

        val placeholdersConfig = readConfig(path.resolve("placeholders.yml"), PlaceholdersConfig::class.java)

        ld.addGlobalStaticPlaceholders(placeholdersConfig.staticPlaceholders)
        ld.addCustomColorTags(placeholdersConfig.customColorTags)

        val tagsStr = placeholdersConfig.customColorTags.entries.toList().joinToString(", ") {
            "<${it.key}>${it.key}</${it.key}>"
        }

        ComponentLogger.logger().info(
            ILocaleEnum.process(
                "Loaded ${placeholdersConfig.customColorTags.size} custom color tags: $tagsStr"
            )
        )

        return locales
    }
}