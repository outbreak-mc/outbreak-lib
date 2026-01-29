package space.outbreak.lib.v2.utils.db

import MigrationUtils
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.semver4j.Semver
import org.slf4j.Logger
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
        driverClassName = "org.sqlite.JDBC"
        jdbcUrl = "jdbc:sqlite:${file}?foreign_keys=on"
        jdbcUrl = "jdbc:sqlite:${file}?foreign_keys=on"
        maximumPoolSize = 5
        isAutoCommit = true
        extraConfig?.invoke(this)
    }

    return Database.connect(HikariDataSource(conf))
}

/**
 * Создаёт in-memory sqlite базу данных с именем [name] По умолчанию включает `foreign_keys`.
 * */
fun inMemorySqlite(name: String = "memdb1", extraConfig: ((HikariConfig) -> Unit)? = null): Database {
    val conf = HikariConfig().apply {
        jdbcUrl = "jdbc:sqlite:file:${name}?mode=memory&cache=shared&foreign_keys=on"
        driverClassName = "org.sqlite.JDBC"
        maximumPoolSize = 5
        isAutoCommit = true
        extraConfig?.invoke(this)
    }

    return Database.connect(HikariDataSource(conf))
}

private const val VERSION = "version"

/** Таблица для хранения данных, нужных для отслеживания версии схемы БД */
class DbVersionMetaTable(name: String) : IntIdTable(name) {
    val key = varchar("key", 32).index()
    val value = text("data")
}

/**
 * Создаёт или обновляет необходимые таблицы в базе данных.
 *
 * Сравнивает версию в таблице метаданных с [latestVersionStr].
 * Если найденная версия ниже или отсутствует, выполняется миграция.
 *
 * @param db подключение к базе данных
 * @param metaTable объект [DbVersionMetaTable] с нужным названием
 * @param logger логгер для сообщений о том, что происходит с базой данных в процессе
 * @param name любое человекопонятное название, используемое в сообщениях логгера. Например, название плагина.
 * @param tables таблицы, которые нужно создать или привести к нужному виду
 * @param latestVersionStr актуальная версия в виде строки вида 1.0.0 (обязательно все три числа)
 * @param fallbackVersionStr если таблица метаданных не найдена, считать, что БД такой версии.
 * */
fun checkAndInitializeTables(
    db: Database,
    metaTable: DbVersionMetaTable,
    logger: Logger,
    name: String,
    tables: Collection<Table>,
    latestVersionStr: String,
    fallbackVersionStr: String = "1.0.0",
    migrateIfUnstable: Boolean
) {
    val latestVersion: Semver = Semver.parse(latestVersionStr)!!
    val fallbackVersion: Semver = Semver.parse(fallbackVersionStr)!!

    fun Transaction.getCurrentDBVersion(): Semver? {
        return metaTable.select(metaTable.value).where { metaTable.key eq VERSION }.firstOrNull()
            ?.let { Semver.parse(it[metaTable.value]) }
    }

    var initNeeded = false
    val ver = transaction(db) {
        if (!metaTable.exists()) {
            initNeeded = true
            logger.warn("Metadata table for $name database does not exist")
            fallbackVersion
        } else {
            val foundVer = getCurrentDBVersion()
            if (foundVer == null) {
                logger.warn(
                    "Unable to retrieve the version of $name database from metadata table. " +
                            "The record does not exist or the value is malformatted."
                )
                initNeeded = true
                fallbackVersion
            } else {
                if (foundVer.isLowerThan(latestVersion)) {
                    logger.warn("The version of the $name database schema is lower than latest $foundVer < $latestVersion.")
                    initNeeded = true
                } else if (migrateIfUnstable && !foundVer.isGreaterThan(latestVersion) && !latestVersion.isStable) {
                    logger.warn("The version of the $name ($latestVersion) is not stable. Database will be migrated.")
                    initNeeded = true
                }
                foundVer
            }
        }
    }

    if (initNeeded) {
        if (ver.isGreaterThan(latestVersion)) {
            logger.error(
                "$name database schema version is greater than expected version ($ver > $latestVersion)! " +
                        "This means that $name is outdated and something might break!"
            )
            return
        }

        transaction(db) {
            logger.warn("Trying to migrate $name database: $ver -> $latestVersion")

            MigrationUtils.statementsRequiredForDatabaseMigration(
                metaTable, *tables.toTypedArray()
            ).forEach { exec(it) }

            val existingVersion = metaTable.select(metaTable.value).where { metaTable.key eq VERSION }.firstOrNull()
            if (existingVersion == null)
                metaTable.insert {
                    it[metaTable.key] = VERSION
                    it[metaTable.value] = latestVersion.toString()
                }
            else
                metaTable.update({ metaTable.key eq VERSION }) { it[metaTable.value] = latestVersion.toString() }

            logger.info("$name database migration complete")
        }
    }
}