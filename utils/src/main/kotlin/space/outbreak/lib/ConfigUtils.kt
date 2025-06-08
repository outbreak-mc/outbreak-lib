package space.outbreak.lib

import com.fasterxml.jackson.core.type.TypeReference
import space.outbreak.lib.locale.LocaleData
import space.outbreak.lib.locale.PlaceholdersConfig
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

class ConfigUtils(
    private val dataDir: Path,
) {
    /**
     * Находит в ресурсах файл [resourcePath], распаковывает его в папку
     * плагина, читает его как yaml и парсит в объект типа [type]
     * */
    fun <T> readConfig(resourcePath: String, type: Class<T>): T {
        return yamlMapper.readValue(extractAndGetResourceFile(resourcePath), type)
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
    fun loadLocalesFolder(ld: LocaleData): List<String> {
        val msgsPath = "/messages"
        LocaleData.clear()

        Res.extractResourcesFolder(msgsPath, dataDir.toFile())

        val locales = readLocaleFiles { lang, data ->
            LocaleData.load(lang, data)
        }

        if (locales.isEmpty())
            throw FileNotFoundException("No locales found neither in resources nor in plugin folder${msgsPath}!")

        val placeholdersConfig = readConfig("${msgsPath}/placeholders.yml", PlaceholdersConfig::class.java)

        LocaleData.addGlobalStaticPlaceholders(placeholdersConfig.staticPlaceholders)
        LocaleData.addCustomColorTags(placeholdersConfig.customColorTags)

        return locales
    }

    /**
     * Распаковывает из ресурсов файл [file] в папку плагина, если его не существует,
     * и читает данные из него в [LocaleData] как язык [lang]. Существующие данные этого
     * языка предварительно очищаются.
     * */
    fun readLocale(file: String = "locale.yml", lang: String) {
        LocaleData.removeLang(lang)
        Res.extractResourcesFolder(file, dataDir.toFile())
        LocaleData.load(lang, dataDir.resolve(file).toFile())
    }
}