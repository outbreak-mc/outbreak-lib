package space.outbreak.lib.locale.db

import MigrationUtils
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import space.outbreak.lib.locale.GlobalLocaleData
import space.outbreak.lib.locale.LocaleData
import java.util.*

val DEFAULT_TABLE_NAMES = TableNamesSystem()
const val CURRENT_LOCALE_DB_SCHEMA_VERSION = 2

internal object MetaKeys {
    const val VERSION = "version"
}

class LocaleDb(
    private val db: Database,
    private val localeData: LocaleData = GlobalLocaleData,
    private val tables: TableNamesSystem = DEFAULT_TABLE_NAMES,
) {
    /**
     * Создаёт или обновляет необходимые таблицы в базе данных.
     *
     * Сравнивает версию в таблице метаданных с актуальной версией для текущей библиотеки.
     * Если версия ниже, выполняется миграция.
     * */
    fun initDatabaseTables() {
        val logger = LoggerFactory.getLogger("OutbreakLib")
        val mt = tables.tables.meta

        val v = transaction(db) {
            try {
                mt.select(mt.value).where { mt.key eq MetaKeys.VERSION }.firstOrNull()
                    ?.let { it[mt.value].toInt() } ?: -1
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

                val existingVersion =
                    mt.select(mt.value).where { mt.key eq MetaKeys.VERSION }
                        .firstOrNull()
                if (existingVersion == null)
                    mt.insert {
                        it[mt.key] = MetaKeys.VERSION
                        it[mt.value] = CURRENT_LOCALE_DB_SCHEMA_VERSION.toString()
                    }
                else
                    mt.update({ mt.key eq MetaKeys.VERSION }) {
                        it[mt.value] = CURRENT_LOCALE_DB_SCHEMA_VERSION.toString()
                    }
            }
        } else if (v > CURRENT_LOCALE_DB_SCHEMA_VERSION) {
            logger.warn(
                "OutbreakLib locale database schema version is greater than current " +
                        "OutbreakLib's (${v} > ${CURRENT_LOCALE_DB_SCHEMA_VERSION}). " +
                        "This means that this plugin is outdated and something might break."
            )
        }
    }

    private fun normalizeNamespace(ns: String?): String {
        if (ns.isNullOrBlank())
            return "*"
        return ns
    }

    private fun Transaction.loadLocale(namespaces: Collection<String>, server: String?) {
        tables.tables.locale.also { t ->
            val q = t.selectAll()
            if (!namespaces.contains("*"))
                q.andWhere {
                    (t.namespace inList namespaces) or (t.namespace eq "*") or (t.namespace.isNull() or (t.namespace eq ""))
                }
            if (!server.isNullOrBlank())
                q.andWhere { t.server.isNull() or (t.server eq "*") or (t.server eq server) }

            val sortedLocaleData = mutableMapOf<String, MutableMap<String, MutableMap<String, String>>>()

            for (row in q) {
                val ns = normalizeNamespace(row[t.namespace])
                val lang = row[t.lang]
                val key = row[t.key]
                val value = row[t.value]
                sortedLocaleData.getOrPut(ns) { mutableMapOf() }.getOrPut(lang) { mutableMapOf() }[key] = value
            }

            for ((ns, data) in sortedLocaleData)
                for ((lang, keyValue) in data)
                    localeData.load(Locale.of(lang), ns, keyValue)
        }
    }

    /**
     * Загружает всё: переводы, цветовые теги и плейсхолдеры сразу из всех пространств имён
     * */
    fun loadAllFromDB(
        namespaces: Collection<String> = listOf("*"),
        server: String? = null,
    ) {


        transaction(db) {
            loadLocale(namespaces, server)

            ////////////////////////////////////////////////////////
            /////////////////// Custom color tags //////////////////
            ////////////////////////////////////////////////////////
            tables.tables.customColorTag.also { t ->
                val q = t.selectAll()
                if (!namespaces.contains("*"))
                    q.andWhere { (t.namespace inList namespaces) or (t.namespace eq "*") or (t.namespace.isNull() or (t.namespace eq "")) }
                val sortedData = mutableMapOf<String, MutableMap<String, String>>()
                for (row in q) {
                    sortedData.getOrPut(normalizeNamespace(row[t.namespace])) { mutableMapOf() }[row[t.tag]] =
                        row[t.hex]
                }
                for ((ns, data) in sortedData)
                    localeData.addCustomColorTags(data)
            }

            ////////////////////////////////////////////////////////
            ////////////////// Static placeholders /////////////////
            ////////////////////////////////////////////////////////
//            tables.tables.placeholder.also { t ->
//                val q = t.selectAll()
//                if (!namespaces.contains("*"))
//                    q.andWhere { (t.namespace inList namespaces) or (t.namespace eq "*") or (t.namespace.isNull() or (t.namespace eq "")) }
//                if (!server.isNullOrBlank())
//                    q.andWhere { t.server.isNull() or (t.server eq "*") or (t.server eq server) }
//
//                val sortedDataLS = mutableMapOf<String, MutableMap<String, MutableMap<String, String>>>()
//                val sortedDataGlobal = mutableMapOf<String, MutableMap<String, String>>()
//
//                for (row in q) {
//                    val ns = normalizeNamespace(row[t.namespace])
//                    val lang = row[t.lang]
//                    val placeholder = row[t.placeholder]
//                    val value = row[t.value]
//
//                    if (lang != null) {
//                        sortedDataLS.getOrPut(ns) { mutableMapOf() }
//                            .getOrPut(lang) { mutableMapOf() }[placeholder] = value
//                    } else {
//                        sortedDataGlobal.getOrPut(ns) { mutableMapOf() }[placeholder] = value
//                    }
//                }
//
//                for ((ns, data) in sortedDataLS) {
//                    for ((lang, placeholders) in data)
//                        localeData.addPlaceholders(ns, lang, placeholders)
//                }
//                for ((ns, placeholders) in sortedDataGlobal)
//                    this@loadAllFromDB.addPlaceholders(ns, null, placeholders)
//            }
        }
    }
}