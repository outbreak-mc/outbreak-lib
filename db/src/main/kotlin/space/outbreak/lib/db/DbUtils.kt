package space.outbreak.lib.db

import MigrationUtils
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import space.outbreak.lib.locale.LocaleData
import java.io.File
import java.util.*

val DEFAULT_TABLE_NAMES = TableNamesSystem()

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
 * Параметры [localeTable] и [customColorTagTable] позволяют пересоздать
 * таблицы с другими
 * */
fun LocaleData.initDatabaseTables(
    db: Database,
    tableNames: TableNamesSystem = DEFAULT_TABLE_NAMES,
) {
    transaction(db) {
        MigrationUtils.statementsRequiredForDatabaseMigration(
            tableNames.tables.locale,
            tableNames.tables.customColorTag,
            tableNames.tables.placeholder,
        ).forEach { exec(it) }
    }
}

/**
 * Загружает переводы из базы данных
 *
 * @param db подключение к бд
 * @param namespace пространство имён - колонка в таблице, определяющая
 *  принадлежность к определённому плагину. Пространство `null` считается
 *  глобальным и загружается всегда.
 * @param tableNames свои названия таблиц, если используется.
 *  Использовать свою систему названий таблиц крайне не рекомендуется.
 * */
fun LocaleData.loadFromDB(
    db: Database,
    namespace: String?,
    tableNames: TableNamesSystem = DEFAULT_TABLE_NAMES,
) = loadFromDB(db, if (namespace != null) listOf(namespace) else emptyList(), tableNames)

/**
 * Загружает переводы из базы данных
 *
 * @param db подключение к бд
 * @param namespaces список пространств имён для загрузки. Пространство
 * имён - это колонка в таблице, определяющая принадлежность к определённому
 * плагину. Пространство `null` считается глобальным и загружается всегда.
 * @param tableNames свои названия таблиц, если используется.
 *  Использовать свою систему названий таблиц крайне не рекомендуется.
 * */
fun LocaleData.loadFromDB(
    db: Database,
    namespaces: Iterable<String>,
    tableNames: TableNamesSystem = DEFAULT_TABLE_NAMES,
) {
    transaction(db) {
        val table = tableNames.tables.locale
        val languages = table.select(table.lang)
            .where { table.namespace.isNull() or (table.namespace inList namespaces) }
            .groupBy(table.lang).map { it[table.lang] }

        for (lang in languages) {
            val data = table.selectAll().where {
                (table.namespace inList namespaces) and (table.lang eq lang)
            }.associate {
                val key = it[table.key]
                    .uppercase()
                    .replace(".", "__")
                    .replace("-", "_")
                val value = it[table.value]

                key to value
            }
            this@loadFromDB.load(lang, data)
        }
    }
}

/**
 * Загружает кастомные цветовые теги из базы данных
 *
 * @param db подключение к бд
 * @param namespaces список пространств имён для загрузки. Пространство
 * имён - это колонка в таблице, определяющая принадлежность к определённому
 * плагину. Пространство `null` считается глобальным и загружается всегда.
 * @param tableNames свои названия таблиц, если используется.
 *  Использовать свою систему названий таблиц крайне не рекомендуется.
 * */
fun LocaleData.loadCustomColorTagsFromDB(
    db: Database,
    namespaces: Iterable<String>,
    tableNames: TableNamesSystem = DEFAULT_TABLE_NAMES,
) {
    transaction(db) {
        val table = tableNames.tables.customColorTag
        val data = table.selectAll().where {
            (table.namespace.isNull()) or (table.namespace inList namespaces)
        }.associate {
            it[table.tag] to it[table.hex]
        }
        this@loadCustomColorTagsFromDB.addCustomColorTags(data)
    }
}

/**
 * Загружает кастомные цветовые теги из базы данных
 *
 * @param db подключение к бд
 * @param namespace пространство имён - колонка в таблице, определяющая
 *  принадлежность к определённому плагину. Пространство `null` считается
 *  глобальным и загружается всегда.
 * @param tableNames свои названия таблиц, если используется.
 *  Использовать свою систему названий таблиц крайне не рекомендуется.
 * */
fun LocaleData.loadCustomColorTagsFromDB(
    db: Database,
    namespace: String?,
    tableNames: TableNamesSystem = DEFAULT_TABLE_NAMES,
) = loadCustomColorTagsFromDB(db, if (namespace != null) listOf(namespace) else emptyList(), tableNames)

/**
 * Загружает статические плейсхолдеры из базы данных
 *
 * @param db подключение к бд
 * @param namespaces список пространств имён для загрузки. Пространство
 * имён - это колонка в таблице, определяющая принадлежность к определённому
 * плагину. Пространство `null` считается глобальным и загружается всегда.
 * @param tableNames свои названия таблиц, если используется.
 *  Использовать свою систему названий таблиц крайне не рекомендуется.
 * */
fun LocaleData.loadPlaceholdersFromDB(
    db: Database,
    namespaces: Iterable<String>,
    tableNames: TableNamesSystem = DEFAULT_TABLE_NAMES,
) {
    transaction(db) {
        val t = tableNames.tables.placeholder
        val data = t.selectAll().where { t.namespace.isNull() or (t.namespace inList namespaces) }

        val langSpecific = mutableMapOf<String, MutableMap<String, String>>()
        val global = mutableMapOf<String, String>()

        for (row in data) {
            val lang = row[t.lang]
            val placeholder = row[t.placeholder]
            val value = row[t.value]

            if (lang == null) {
                global[placeholder] = value
            } else {
                langSpecific.getOrPut(lang) { mutableMapOf() }[placeholder] = value
            }
        }

        this@loadPlaceholdersFromDB.addGlobalStaticPlaceholders(global)
        for ((lang, placeholders) in langSpecific)
            this@loadPlaceholdersFromDB.addLangSpecificStaticPlaceholders(lang, placeholders)
    }
}

/**
 * Загружает статические плейсхолдеры из базы данных
 *
 * @param db подключение к бд
 * @param namespace пространство имён - колонка в таблице, определяющая
 *  принадлежность к определённому плагину. Пространство `null` считается
 *  глобальным и загружается всегда.
 * @param tableNames свои названия таблиц, если используется.
 *  Использовать свою систему названий таблиц крайне не рекомендуется.
 * */
fun LocaleData.loadPlaceholdersFromDB(
    db: Database,
    namespace: String?,
    tableNames: TableNamesSystem = DEFAULT_TABLE_NAMES,
) = loadPlaceholdersFromDB(db, if (namespace != null) listOf(namespace) else emptyList(), tableNames)