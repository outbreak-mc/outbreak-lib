package space.outbreak.lib.utils.locale

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.minimessage.translation.Argument
import space.outbreak.lib.utils.locale.pair.LocalePairBase
import java.util.*

interface IFormatter {
    val data: LocaleData

    /**
     * Подставляет плейсхолдеры из карты [placeholders]` в строку [str],
     * возвращает получившуюся строку.
     *
     * Производительнее, чем просто `replace()` и поддерживает экранирование
     * символа `%` при помощи `\`.
     * */
    fun stringReplaceAll(str: String, placeholders: Map<String, Any?>): String
    fun stringReplaceAll(str: String, vararg placeholders: LocalePairBase): String
    fun byPath(path: String, lang: Locale?): String?
    fun _process(
        text: String,
        mapStrings: Map<String, Any?>,
        mapComps: Map<String, Component>,
    ): Component

    /**
     * Оборачивает компонент в компонент с явно отключенным курсивом.
     * Полезно, чтобы убирать курсив из описаний и названий предметов.
     * */
    fun deitalize(comp: Component): Component
    fun processAndDeitalize(text: String, lang: Locale? = null, vararg placeholders: LocalePairBase): Component
    fun process(text: String, lang: Locale?, replacing: Collection<LocalePairBase>): Component
    fun process(text: String, lang: Locale?, vararg replacing: LocalePairBase): Component
    fun process(text: String, vararg replacing: LocalePairBase): Component

    companion object {
        fun processArgs(vararg replacing: LocalePairBase): Array<ComponentLike> {
            return Array(replacing.size) { i ->
                when (val el = replacing[i]) {
                    is LocalePairBase.component -> Argument.component(el.key, el.value)
                    is LocalePairBase.string -> Argument.string(el.key, el.value)
                }
            }
        }
    }
}