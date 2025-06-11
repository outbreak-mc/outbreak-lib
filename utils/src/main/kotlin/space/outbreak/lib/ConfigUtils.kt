package space.outbreak.lib

import org.yaml.snakeyaml.Yaml
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
    private val yaml = Yaml()

    /**
     * Находит в ресурсах файл [resourcePath], распаковывает его в папку
     * плагина, читает его как yaml и парсит в объект типа [type]
     * */
    fun <T> readConfig(resourcePath: String, type: Class<T>): T {
        return yaml.loadAs(extractAndGetResourceFile(resourcePath).inputStream(), type)
    }

    // fun writeConfig(resourcePath: String, data: Any) {
    //     yamlMapper.writeValue(extractAndGetResourceFile(resourcePath), data)
    // }

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
            ?.forEach { f ->
                val (name, _) = f.name.split(".", limit = 2)
                func(name, yaml.load(f.inputStream()))
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

        LocaleData.addGlobalStaticPlaceholders(placeholdersConfig.`static-placeholders`)
        LocaleData.addCustomColorTags(placeholdersConfig.`custom-color-tags`)

        return locales
    }

    /**
     * Распаковывает из ресурсов файл [file] в папку плагина, если его не существует,
     * и читает данные из него в [LocaleData] как язык [lang]. Существующие данные этого
     * языка предварительно очищаются.
     * */
    fun loadSingleLocaleFile(file: String = "locale.yml", lang: String) {
        LocaleData.removeLang(lang)
        Res.saveResource(file, dataDir.toFile(), false)
        LocaleData.load(lang, dataDir.resolve(file).toFile())
    }
}