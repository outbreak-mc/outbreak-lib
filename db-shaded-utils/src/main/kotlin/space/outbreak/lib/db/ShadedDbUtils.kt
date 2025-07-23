package space.outbreak.lib.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import java.io.File
import java.util.*

/** Подключается к базе данных на основе параметров из `.properties`-файла. */
fun connectToDB(configFile: File): Database {
    val props = Properties()
    props.load(configFile.inputStream())
    return Database.connect(HikariDataSource(HikariConfig(props)))
}

/** Метод для простого и удобного подключения к SQLite.
 * По умолчанию включает `foreign_keys`. */
fun connectToSqlite(file: File, extraConfig: ((HikariConfig) -> Unit)? = null): Database {
    val conf = HikariConfig().apply {
        jdbcUrl = "jdbc:sqlite:${file}?foreign_keys=on"
        driverClassName = "org.sqlite.JDBC"
        maximumPoolSize = 5
        isAutoCommit = true
        extraConfig?.invoke(this)
    }

    return Database.connect(HikariDataSource(conf))
}
