package space.outbreak.lib.db

import MigrationUtils
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import space.outbreak.lib.locale.LocaleDataManager
import java.io.File
import java.util.*

val DEFAULT_TABLE_NAMES = TableNamesSystem()
const val CURRENT_LOCALE_DB_SCHEMA_VERSION = 1

internal object MetaKeys {
    const val VERSION = "version"
}

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

/**
 * Создаёт или обновляет необходимые таблицы в базе данных.
 *
 * Сравнивает версию в таблице метаданных с актуальной версией для текущей библиотеки.
 * Если версия ниже, выполняется миграция.
 * */
fun LocaleDataManager.initDatabaseTables(
    db: Database,
    tables: TableNamesSystem = DEFAULT_TABLE_NAMES,
) {
    val logger = LoggerFactory.getLogger("OutbreakLib")
    val mt = tables.tables.meta

    val v = transaction(db) {
        try {
            mt.select(mt.value).where { mt.key eq MetaKeys.VERSION }.firstOrNull()?.let { it[mt.value] } ?: -1
        } catch (e: Exception) {
            logger.error("Database check error: ${e.message}")
            -1
        }
    }

    if (v < CURRENT_LOCALE_DB_SCHEMA_VERSION) {
        if (v == -1)
            logger.warn("Database schema does not exist or is of a very unsupported version. Trying to migrate.")
        else
            logger.warn(
                "Database schema version is lower than supported ${v} < ${CURRENT_LOCALE_DB_SCHEMA_VERSION}. " +
                        "Trying to migrate."
            )
        transaction(db) {
            MigrationUtils.statementsRequiredForDatabaseMigration(
                tables.tables.meta,
                tables.tables.locale,
                tables.tables.customColorTag,
                tables.tables.placeholder,
            ).forEach { exec(it) }

            val existingVersion = mt.select(mt.value).where { mt.key eq MetaKeys.VERSION }.firstOrNull()
            if (existingVersion == null)
                mt.insert {
                    it[mt.key] = MetaKeys.VERSION
                    it[mt.value] = CURRENT_LOCALE_DB_SCHEMA_VERSION
                }
            else
                mt.update({ mt.key eq MetaKeys.VERSION }) { it[mt.value] = CURRENT_LOCALE_DB_SCHEMA_VERSION }
        }
    } else if (v > CURRENT_LOCALE_DB_SCHEMA_VERSION) {
        logger.warn(
            "OutbreakLib locale database schema version is greater than current " +
                    "OutbreakLib's (${v} > ${CURRENT_LOCALE_DB_SCHEMA_VERSION}). " +
                    "This means that this plugin is outdated and something might break."
        )
    }
}

/**
 * Загружает всё: переводы, цветовые теги и плейсхолдеры сразу из всех пространств имён
 * */
fun LocaleDataManager.loadAllFromDB(
    db: Database,
    namespaces: Collection<String>,
    tables: TableNamesSystem = DEFAULT_TABLE_NAMES,
) {
    fun normalizeNamespace(ns: String?): String {
        if (ns.isNullOrBlank())
            return "*"
        return ns
    }

    transaction(db) {
        ////////////////////////////////////////////////////////
        //////////////////////// Locale ////////////////////////
        ////////////////////////////////////////////////////////
        tables.tables.locale.also { t ->
            val lQuery = t.selectAll()
            if (!namespaces.contains("*"))
                lQuery.andWhere {
                    (t.namespace inList namespaces) or (t.namespace eq "*") or (t.namespace.isNull() or (t.namespace eq ""))
                }

            val sortedLocaleData = mutableMapOf<String, MutableMap<String, MutableMap<String, String>>>()

            for (row in lQuery) {
                val ns = normalizeNamespace(row[t.namespace])
                val lang = row[t.lang]
                val key = row[t.key]
                val value = row[t.value]
                sortedLocaleData.getOrPut(ns ?: "*") { mutableMapOf() }.getOrPut(lang) { mutableMapOf() }[key] = value
            }

            for ((ns, data) in sortedLocaleData)
                for ((lang, keyValue) in data)
                    this@loadAllFromDB.load(ns, lang, keyValue)
        }

        ////////////////////////////////////////////////////////
        /////////////////// Custom color tags //////////////////
        ////////////////////////////////////////////////////////
        tables.tables.customColorTag.also { t ->
            val q = t.selectAll()
            if (!namespaces.contains("*"))
                q.andWhere { (t.namespace inList namespaces) or (t.namespace eq "*") or (t.namespace.isNull() or (t.namespace eq "")) }
            val sortedData = mutableMapOf<String, MutableMap<String, String>>()
            for (row in q) {
                sortedData.getOrPut(normalizeNamespace(row[t.namespace])) { mutableMapOf() }[row[t.tag]] = row[t.hex]
            }
            for ((ns, data) in sortedData)
                this@loadAllFromDB.addCustomColorTags(ns, data)
        }


        ////////////////////////////////////////////////////////
        ////////////////// Static placeholders /////////////////
        ////////////////////////////////////////////////////////
        tables.tables.placeholder.also { t ->
            val q = t.selectAll()
            if (!namespaces.contains("*"))
                q.andWhere { (t.namespace inList namespaces) or (t.namespace eq "*") or (t.namespace.isNull() or (t.namespace eq "")) }

            val sortedDataLS = mutableMapOf<String, MutableMap<String, MutableMap<String, String>>>()
            val sortedDataGlobal = mutableMapOf<String, MutableMap<String, String>>()

            for (row in q) {
                val ns = normalizeNamespace(row[t.namespace])
                val lang = row[t.lang]
                val placeholder = row[t.placeholder]
                val value = row[t.value]

                if (lang != null) {
                    sortedDataLS.getOrPut(ns) { mutableMapOf() }
                        .getOrPut(lang) { mutableMapOf() }[placeholder] = value
                } else {
                    sortedDataGlobal.getOrPut(ns) { mutableMapOf() }[placeholder] = value
                }
            }

            for ((ns, data) in sortedDataLS) {
                for ((lang, placeholders) in data)
                    this@loadAllFromDB.addPlaceholders(ns, lang, placeholders)
            }
            for ((ns, placeholders) in sortedDataGlobal)
                this@loadAllFromDB.addPlaceholders(ns, null, placeholders)
        }
    }
}