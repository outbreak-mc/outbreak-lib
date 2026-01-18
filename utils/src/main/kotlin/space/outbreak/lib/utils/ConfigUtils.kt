package space.outbreak.lib.utils

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor
import org.yaml.snakeyaml.representer.Representer
import space.outbreak.lib.locale.GlobalLocaleData
import space.outbreak.lib.locale.LocaleData
import space.outbreak.lib.locale.ofExactLocale
import space.outbreak.lib.utils.resapi.Res
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.*

class ConfigUtils(
    private val dataDir: Path,
    private val cl: ClassLoader,
) {
    private val yamlRepr = Representer(DumperOptions()).apply {
        propertyUtils.isSkipMissingProperties = true
    }
    private val yaml = Yaml(
        CustomClassLoaderConstructor(cl, LoaderOptions()),
        yamlRepr
    )
    private val res = Res(cl)

    /**
     * Находит в ресурсах файл [resourcePath], распаковывает его
     * в папку плагина, читает его как yaml и парсит в объект типа [type].
     * Если файла в ресурсах нет, возвращает null.
     * */
    fun <T> readConfig(resourcePath: String, type: Class<T>): T? {
        val input = extractAndGetResourceFile(resourcePath) ?: return null
        return Yaml(CustomClassLoaderConstructor(type.classLoader, LoaderOptions()), yamlRepr)
            .loadAs(input.inputStream(), type)
    }

    // fun writeConfig(resourcePath: String, data: Any) {
    //     yamlMapper.writeValue(extractAndGetResourceFile(resourcePath), data)
    // }

    /**
     * Извлекает файл или папку из ресурсов jar в папку плагина и
     * возвращает [File] или null, если файла в ресурсах нет
     * */
    fun extractAndGetResourceFile(path: String): File? {
        val outputFile = dataDir.absolute().resolve(path.trimStart('/'))

        if (!outputFile.exists()) {
            val resourcePath = path.trimStart('/')
            val inputStream = object {}.javaClass.classLoader.getResourceAsStream(resourcePath) ?: return null

            if (outputFile.isDirectory())
                outputFile.createDirectories()
            else if (!outputFile.parent.exists())
                outputFile.parent.createDirectories()

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
    fun loadLocalesFolder(namespace: String, ld: LocaleData = GlobalLocaleData): List<String> {
        val msgsPath = "messages"
        ld.clear()

        res.extract(msgsPath, dataDir.toFile(), false)

        val locales = readLocaleFiles { lang, data ->
            ld.load(ofExactLocale(lang), namespace, data)
        }

        if (locales.isEmpty())
            throw FileNotFoundException("No locales found neither in resources nor in plugin folder${msgsPath}!")

        readConfig("${msgsPath}/placeholders.yml", PlaceholdersConfig::class.java)?.also { placeholdersConfig ->
            ld.addCustomColorTags(placeholdersConfig.`custom-color-tags`)
        }

        // ld.addPlaceholders(null, placeholdersConfig.`static-placeholders`)

        return locales
    }

    /**
     * Распаковывает из ресурсов файл [file] в папку плагина, если его не существует,
     * и читает данные из него в [space.outbreak.lib.utils.locale.LocaleDataManagerBase] как язык [lang]. Существующие данные этого
     * языка предварительно очищаются.
     * */
    fun loadSingleLocaleFile(ld: LocaleData, namespace: String, file: String = "locale.yml", lang: Locale? = null) {
        val l = lang ?: ld.defaultLang
        res.saveResource(file, dataDir.toFile(), false)
        val map: Map<String, Any> = yaml.load(dataDir.resolve(file).inputStream())
        ld.load(l, namespace, map)
    }
}

