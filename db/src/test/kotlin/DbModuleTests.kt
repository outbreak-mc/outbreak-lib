import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import space.outbreak.lib.db.DEFAULT_TABLE_NAMES
import space.outbreak.lib.db.initDatabaseTables
import space.outbreak.lib.db.loadAllFromDB
import space.outbreak.lib.locale.LocaleDataManager
import space.outbreak.lib.locale.pair.means
import kotlin.time.measureTime

class DbModuleTests {
    companion object {
        val database by lazy {
            Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        }

        val NAMESPACE = "outbreaklib.test"

        @JvmStatic
        fun prepareTestDB() {
            LocaleDataManager.initDatabaseTables(database)
            transaction(database) {
                DEFAULT_TABLE_NAMES.tables.locale.let { t ->
                    fun insert(key: String, value: String, lang: String = "ru_RU") {
                        t.insert {
                            it[t.namespace] = NAMESPACE
                            it[t.lang] = lang
                            it[t.key] = key
                            it[t.value] = value
                        }
                    }

                    insert("simplest", "<rainbow>Простое сообщение без плейсхолдеров и без `.`/`-`")
                    insert("path.with.three.dots-and-hyphens", "<green>Путь с точками и тире")
                    insert(
                        "msg.with-placeholders",
                        "Статический: %static-placeholder%<newline>" +
                                "Подстановка строк: %string%<newline>" +
                                "Подстановка компонентов: %component%"
                    )
                    insert("custom-color-tags", "<pink>Кастомный розовый</pink> | <lb>Кастомный голубой</lb>")
                }

                DEFAULT_TABLE_NAMES.tables.placeholder.let { t ->
                    t.insert {
                        it[t.lang] = "ru_RU"
                        it[t.namespace] = NAMESPACE
                        it[t.placeholder] = "static-placeholder"
                        it[t.value] =
                            "<yellow>Статический плейсхолдер с <pink>кастомным цветом</pink>. Вот это да!</yellow>"
                    }
                }

                DEFAULT_TABLE_NAMES.tables.customColorTag.also { t ->
                    t.insert {
                        it[t.namespace] = NAMESPACE
                        it[t.tag] = "pink"
                        it[t.hex] = "#f7c8e5"
                    }
                    t.insert {
                        it[t.namespace] = NAMESPACE
                        it[t.tag] = "lb"
                        it[t.hex] = "#00bbff"
                    }
                }
            }
        }

        private val ansiSerializer = ANSIComponentSerializer.ansi()

        fun printansi(comp: Component) {
            println(ansiSerializer.serialize(comp))
        }
    }

    @Test
    fun tablesInitialization() {
        // Сначала заставим подключение инициализироваться, чтобы убрать погрешность из-за этого
        transaction(database) { exec("SELECT 1;") }

        val firstTime = measureTime { LocaleDataManager.initDatabaseTables(database) }.inWholeMilliseconds
        val secondTime = measureTime { LocaleDataManager.initDatabaseTables(database) }.inWholeMilliseconds
        val thirdTime = measureTime { LocaleDataManager.initDatabaseTables(database) }.inWholeMilliseconds

        println("First initialization: ${firstTime} ms")
        println("Second initialization: ${secondTime} ms")
        println("Third initialization: ${thirdTime} ms")

        // Последующие инициализации без миграции должны быть очень быстрыми
        assert((secondTime + thirdTime) <= (firstTime / 5))
    }

    @Test
    fun testenumlocale() {
        prepareTestDB()
        LocaleDataManager.loadAllFromDB(database, listOf(NAMESPACE))

        printansi(L.SIMPLEST.comp())
        printansi(L.PATH__WITH__THREE__DOTS_AND_HYPHENS.comp())
        printansi(
            L.MSG__WITH_PLACEHOLDERS.comp(
                "string" means "<aqua>Строка</aqua> (<green>ОК</green>)",
                "component" means miniMessage().deserialize("<dark_purple>Компонент</dark_purple> (<green>ОК</green>)")
            )
        )
        printansi(
            L.CUSTOM_COLOR_TAGS.comp()
        )
    }
}