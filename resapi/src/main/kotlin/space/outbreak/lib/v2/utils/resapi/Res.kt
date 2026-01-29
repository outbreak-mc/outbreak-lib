package space.outbreak.lib.v2.utils.resapi

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor
import org.yaml.snakeyaml.representer.Representer
import java.io.*
import java.net.URL
import java.nio.file.*
import java.util.stream.Stream
import kotlin.io.path.isDirectory
import kotlin.io.path.relativeTo

@Suppress("MemberVisibilityCanBePrivate")
class Res(private val cl: ClassLoader) {
    /** Позволяет прочитать файл из ресурсов jar */
    fun resourceAsStream(filename: String): InputStream? {
        try {
            val url: URL = cl.getResource(filename) ?: return null
            val connection = url.openConnection()
            connection.useCaches = false
            return connection.getInputStream()
        } catch (ex: IOException) {
            return null
        }
    }

    /** Возвращает объект [Path], указывающий на ресурс jar */
    fun getResourceAsPath(path: String): Path? {
        val uri = (cl.getResource(path) ?: return null).toURI()
        return try {
            Paths.get(uri)
        } catch (e: FileSystemNotFoundException) {
            // If this is thrown, then it means that we are running the JAR directly (example: not from an IDE)
            val env = mutableMapOf<String, String>()
            FileSystems.newFileSystem(uri, env).getPath(path)
        }
    }

    /** Получает список файлов и папок в ресурсах jar по пути [path] */
    fun listResourceFiles(path: String): Stream<Path>? {
        val dirPath = getResourceAsPath(path) ?: return null
        val p = dirPath.relativeTo(dirPath.root)
        return Files.list(p)
    }

    /** Проверяет, существует ли файл или папка по пути [path] в ресурсах jar */
    fun exists(path: String): Boolean {
        return getResourceAsPath(path) != null
    }

    /**
     * Находит в ресурсах файл [resourcePath], распаковывает его
     * в папку плагина, читает его как yaml и парсит в объект типа [type].
     * Если файла в ресурсах нет, возвращает null.
     * */
    fun <T> readConfig(dataFolder: File, resourcePath: String, type: Class<T>): T? {
        val outPath = dataFolder.resolve(resourcePath)
        extract(resourcePath, outPath)
        val yamlRepr = Representer(DumperOptions()).apply {
            propertyUtils.isSkipMissingProperties = true
        }
        return Yaml(
            CustomClassLoaderConstructor(type.classLoader, LoaderOptions()),
            yamlRepr
        ).loadAs(outPath.inputStream(), type)
    }

    /** Извлекает все ресурсы jar по пути [path] в папку [dst] */
    fun extract(path: String, dst: File, replace: Boolean = false) {
        val resPath = getResourceAsPath(path) ?: throw FileNotFoundException(path)
        if (!resPath.isDirectory()) {
            saveResource(path, dst, replace)
        } else {
            if (!replace && dst.resolve(path).exists())
                return

            listResourceFiles(path)!!.forEach {
                if (!it.isDirectory())
                    saveResourceEmbeddingPath("$it", dst, replace)
                else
                    extract(it.toString(), dst, replace)
            }
        }
    }

    /**
     * Сохраняет файл [resourcePath] из ресурсов в [dst].
     *
     * @param resourcePath путь к файлу в ресурсах. Начинается ли он со слеша - не важно.
     * @param dst папка или файл для сохранения.
     * */
    fun saveResource(resourcePath: String, dst: File, replace: Boolean) {
        val fineResourcePath = resourcePath
            .replace('\\', '/')
            .trimStart('/', ' ')

        require(fineResourcePath.isNotBlank()) { "ResourcePath cannot be null or empty" }

        val `in` = resourceAsStream(fineResourcePath)
            ?: throw IllegalArgumentException("The embedded resource '$fineResourcePath' cannot be found")

        val outFile = if (dst.isDirectory) File(dst, fineResourcePath) else dst

        if (!outFile.parentFile.exists())
            outFile.parentFile.mkdirs()

        if (!outFile.exists() || replace) {
            val out: OutputStream = FileOutputStream(outFile)
            val buf = ByteArray(1024)
            var len: Int
            while ((`in`.read(buf).also { len = it }) > 0) {
                out.write(buf, 0, len)
            }
            out.close()
            `in`.close()
        }
    }

    private fun saveResourceEmbeddingPath(resourcePath: String, dst: File, replace: Boolean) {
        val fineResourcePath = resourcePath
            .replace('\\', '/')
            .trimStart('/')

        require(fineResourcePath != "") { "resourcePath cannot be null or empty" }

        val `in` = resourceAsStream(fineResourcePath)
            ?: throw IllegalArgumentException("The embedded resource '$fineResourcePath' cannot be found")

        val outFile = File(dst, fineResourcePath)

        val lastIndex = fineResourcePath.lastIndexOf('/')

        val outDir = File(dst, fineResourcePath.substring(0, if (lastIndex >= 0) lastIndex else 0))
        if (!outDir.exists())
            outDir.mkdirs()

        if (!outFile.exists() || replace) {
            val out: OutputStream = FileOutputStream(outFile)
            val buf = ByteArray(1024)
            var len: Int
            while ((`in`.read(buf).also { len = it }) > 0) {
                out.write(buf, 0, len)
            }
            out.close()
            `in`.close()
        }
    }
}