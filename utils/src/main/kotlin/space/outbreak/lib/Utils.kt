package space.outbreak.lib

import java.io.File
import java.io.FileInputStream
import java.util.*

/** @return имя основного мира, указанного в `server.properties` */
fun getLevelName(): String {
    val pr = Properties()
    pr.load(FileInputStream(File("server.properties")))
    return pr.getProperty("level-name")
}