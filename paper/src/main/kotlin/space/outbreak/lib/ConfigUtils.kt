package space.outbreak.lib

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import org.bukkit.plugin.java.JavaPlugin
import space.outbreak.lib.locale.ILocaleEnum
import space.outbreak.lib.locale.LocaleData
import space.outbreak.lib.locale.PlaceholdersConfig
import java.io.File
import java.io.FileNotFoundException
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

class ConfigUtils(
    private val dataDir: Path,
) {
    constructor(plugin: JavaPlugin) : this(plugin.dataFolder.toPath())

    val yamlMapper: ObjectMapper = YAMLMapper.builder()
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
    fun <T> readConfig(resourcePath: String, type: Class<T>): T {
        return yamlMapper.readValue(extractAndGetResourceFile(resourcePath), type)
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

    fun writeConfig(resourcePath: String, data: Any) {
        yamlMapper.writeValue(extractAndGetResourceFile(resourcePath), data)
    }

    fun extractAndGetResourceFile(path: String): File {
        val outputFile = dataDir.absolute().resolve(path.trimStart('/'))
        if (outputFile.isDirectory() && !outputFile.exists()) {
            outputFile.createDirectories()
        } else if (!outputFile.isDirectory() && !outputFile.parent.exists()) {
            outputFile.parent.createDirectories()
        }

        if (!outputFile.exists()) {
            val resourcePath = path.trimStart('/')
            val inputStream = object {}.javaClass.classLoader.getResourceAsStream(resourcePath)
                ?: throw FileNotFoundException("Resource not found: ${resourcePath}")
            Files.copy(inputStream, outputFile)
        }

        return outputFile.toFile()
    }


    // fun getResourceFiles(path: String): Stream<Path> {
    //     val resourceUrl = object {}.javaClass.getResource(path)
    //         ?: throw FileNotFoundException("Resource not found: $path")
    //
    //     return try {
    //         val uri = resourceUrl.toURI()
    //
    //         if (uri.scheme == "jar") {
    //             val fileSystem = try {
    //                 FileSystems.newFileSystem(uri, emptyMap<String, Any>())
    //             } catch (e: FileSystemAlreadyExistsException) {
    //                 FileSystems.getFileSystem(uri)
    //             }
    //
    //             val dirPath = fileSystem.getPath(path)
    //             Files.list(dirPath).map {
    //                 it
    //             }
    //         } else {
    //             Files.list(Paths.get(uri)).map { it }
    //         }
    //     } catch (e: IOException) {
    //         throw RuntimeException("Ошибка чтения ресурсов в пути: $path", e)
    //     }
    // }

    private fun readLocaleFiles(
        func: (String, Map<String, Any>) -> Unit,
    ): List<String> {
        val out = mutableListOf<String>()
        dataDir.resolve("messages").resolve("locales").toFile().listFiles()?.filter { it.name.endsWith(".yml") }
            ?.forEach {
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
        val msgsPath = "/messages"
        ld.clear()

        Res.extractResourcesFolder(msgsPath, dataDir.toFile())

        val locales = readLocaleFiles { lang, data ->
            ld.load(lang, data)
        }

        if (locales.isEmpty())
            throw FileNotFoundException("No locales found neither in resources nor in plugin folder${msgsPath}!")

        val placeholdersConfig = readConfig("${msgsPath}/placeholders.yml", PlaceholdersConfig::class.java)

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