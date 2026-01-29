package space.outbreak.lib.v2.locale.db

import net.kyori.adventure.key.Key
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import space.outbreak.lib.v2.locale.ofExactLocale
import space.outbreak.lib.v2.locale.source.ITranslationsSource
import space.outbreak.lib.v2.utils.db.checkAndInitializeTables
import java.util.*

/**
 * Позволяет загружать переводы из SQL базы данных.
 *
 * @param server подразумевается, что разные сервера могут иметь разные переводы. Данный
 *  параметр позволяет отфильтровать переводы по серверу. Пустота/null/"*" - загрузить все сервера
 *  (возможны неожиданные переопределения!)
 * @param namespaces список простраств имён для подгрузки из БД. Пустой список
 *  вернёт пустой результат. Для получения всех существующих пространств имён
 *  можно использовать колекцию с элементом `"*"` (например, `listOf("*")`)
 * @param db подключение к БД. Таблицы должны быть инициализированы заранее.
 *  это можно сделать методом [checkAndInitDatabaseTables].
 * @param tables конфигурация имён таблиц
 * @param logger логгер для возможных предупрежений и уведомлений.
 * @param
 * */
class SQLLocaleSource(
    private val server: String?,
    private val namespaces: Collection<String>,
    private val db: Database,
    private val tables: LocaleTableNamesSystem,
    private val logger: Logger,
) : ITranslationsSource {
    /**
     * Создаёт или обновляет необходимые таблицы в базе данных.
     *
     * Сравнивает версию в таблице метаданных с актуальной версией для текущей библиотеки.
     * Если версия ниже, выполняется миграция.
     * */
    fun checkAndInitDatabaseTables(latestVersion: String, migrateIfUnstable: Boolean) {
        checkAndInitializeTables(
            db,
            tables.tables.meta,
            logger,
            "OutbreakLib::locale",
            listOf(
                tables.tables.locale,
                tables.tables.customColorTag,
                tables.tables.placeholder,
            ),
            latestVersion,
            migrateIfUnstable = migrateIfUnstable
        )
    }

    override fun getAllTranslations(serverName: String): Map<Locale, Map<Key, String>> {
        if (namespaces.isEmpty())
            return mapOf()

        val t = tables.tables.locale

        return transaction(db) {
            val q = t.selectAll()
            if (!namespaces.contains("*"))
                q.andWhere { (t.namespace inList namespaces) or (t.namespace eq "*") or (t.namespace.isNullOrEmpty()) }
            if (!server.isNullOrBlank() && server != "*")
                q.andWhere { t.server.isNull() or (t.server eq "*") or (t.server eq server) }

            val result = mutableMapOf<Locale, MutableMap<Key, String>>()
            for (row in q) {
                val lang = ofExactLocale(row[t.lang])
                val key = Key.key(row[t.namespace], row[t.key])
                val value = row[t.value]
                result.getOrPut(lang) { mutableMapOf() }[key] = value
            }

            result
        }

    }
}