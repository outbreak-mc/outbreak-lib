package space.outbreak.lib

import java.io.*
import java.net.URL
import java.nio.file.*
import java.util.stream.Stream
import kotlin.io.path.isDirectory

@Suppress("MemberVisibilityCanBePrivate")
object Res {
    private val cl get() = object {}.javaClass.classLoader

    fun getResource(filename: String): InputStream? {
        try {
            val url: URL = cl.getResource(filename) ?: return null
            val connection = url.openConnection()
            connection.useCaches = false
            return connection.getInputStream()
        } catch (ex: IOException) {
            return null
        }
    }

    fun getResourceFiles(path: String): Stream<Path> {
        val uri = object {}.javaClass.getResource(path)!!.toURI()
        val dirPath = try {
            Paths.get(uri)
        } catch (e: FileSystemNotFoundException) {
            // If this is thrown, then it means that we are running the JAR directly (example: not from an IDE)
            val env = mutableMapOf<String, String>()
            FileSystems.newFileSystem(uri, env).getPath(path)
        }

        return Files.list(dirPath)
    }

    fun extractResourcesFolder(path: String, toDir: File) {
        getResourceFiles(path).forEach {
            println(it)
            if (!it.isDirectory())
                saveResource("$it", toDir, false)
            else
                extractResourcesFolder(it.toString(), toDir)
        }
    }

    /**
     * Сохраняет файл [resourcePath] из ресурсов в папку [dataFolder]. Если указан путь к ресурсу, находящемуся
     * во вложенной папке, в выходной папке будут созданы недостающие папки.
     *
     * @param resourcePath путь к файлу в ресурсах. Начинается ли он со слеша - не важно.
     * @param dataFolder папка для сохранения
     * */
    fun saveResource(resourcePath: String, dataFolder: File, replace: Boolean) {
        val fineResourcePath = resourcePath
            .replace('\\', '/')
            .trimStart('/')

        require(fineResourcePath != "") { "ResourcePath cannot be null or empty" }

        val `in` = getResource(fineResourcePath)
            ?: throw IllegalArgumentException("The embedded resource '$fineResourcePath' cannot be found")

        val outFile = File(dataFolder, fineResourcePath)
        val lastIndex = fineResourcePath.lastIndexOf('/')
        val outDir = File(dataFolder, fineResourcePath.substring(0, if (lastIndex >= 0) lastIndex else 0))

        if (!outDir.exists()) {
            outDir.mkdirs()
        }

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