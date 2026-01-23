package space.outbreak.lib.locale.db

val DEFAULT_TABLE_NAMES = LocaleTableNamesSystem("outbreaklib_locale")
const val CURRENT_LOCALE_DB_SCHEMA_VERSION = "2.0.0-SNAPSHOT"

//internal object MetaKeys {
//    const val VERSION = "version"
//}
//
//class LocaleDb(
//    private val db: Database,
//    private val logger: Logger,
//    private val metaTableName: String,
//    private val localeData: LocaleData = GlobalLocaleData,
//    private val tables: LocaleTableNamesSystem = DEFAULT_TABLE_NAMES
//) {
//
//
//    /**
//     * Загружает всё: переводы, цветовые теги и плейсхолдеры сразу из всех пространств имён
//     * */
//    fun loadAllFromDB(
//        namespaces: Collection<String> = listOf("*"),
//        server: String? = null,
//    ) {
//
//
//        transaction(db) {
//            loadLocale(namespaces, server)
//
//            ////////////////////////////////////////////////////////
//            /////////////////// Custom color tags //////////////////
//            ////////////////////////////////////////////////////////
//            tables.tables.customColorTag.also { t ->
//                val q = t.selectAll()
//                if (!namespaces.contains("*"))
//                    q.andWhere { (t.namespace inList namespaces) or (t.namespace eq "*") or (t.namespace.isNull() or (t.namespace eq "")) }
//                val sortedData = mutableMapOf<String, MutableMap<String, String>>()
//                for (row in q) {
//                    sortedData.getOrPut(normalizeNamespace(row[t.namespace])) { mutableMapOf() }[row[t.tag]] =
//                        row[t.hex]
//                }
//                for ((ns, data) in sortedData)
//                    localeData.addCustomColorTags(data)
//            }
//
//            ////////////////////////////////////////////////////////
//            ////////////////// Static placeholders /////////////////
//            ////////////////////////////////////////////////////////
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
//        }
//    }
//}