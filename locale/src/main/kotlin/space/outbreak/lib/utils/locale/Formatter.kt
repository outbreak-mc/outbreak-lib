package space.outbreak.lib.utils.locale

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.format.TextDecoration
import org.apache.commons.text.StringSubstitutor
import space.outbreak.lib.utils.locale.pair.LocalePairBase
import java.util.*

class Formatter(override val data: LocaleData) : IFormatter {
    /**
     * Подставляет плейсхолдеры из карты [placeholders]` в строку [str],
     * возвращает получившуюся строку.
     *
     * Производительнее, чем просто `replace()` и поддерживает экранирование
     * символа `%` при помощи `\`.
     * */
    override fun stringReplaceAll(str: String, placeholders: Map<String, Any?>): String {
        val substitutor = StringSubstitutor(placeholders, "%", "%", '\\')
        return substitutor.replace(str)
    }

    override fun stringReplaceAll(str: String, vararg placeholders: LocalePairBase): String {
        val map = placeholders.associate {
            if (it.value is Component)
                throw IllegalArgumentException(
                    "A Component found in the stringReplaceAll method! " +
                            "It is most likely a mistake."
                )
            it.key to it.value
        }
        val substitutor = StringSubstitutor(map, "%", "%", '\\')
        return substitutor.replace(str)
    }

    override fun byPath(path: String, lang: Locale?): String? {
        return data.compiledTree[lang ?: data.defaultLang]?.get(path)
    }

    override fun _process(
        text: String,
        mapStrings: Map<String, Any?>,
        mapComps: Map<String, Component>,
    ): Component {
        var comp = data.serializer.deserialize(stringReplaceAll(text, mapStrings))

        for (entry in mapComps.iterator()) {
            comp = comp.replaceText(
                TextReplacementConfig.builder()
                    .matchLiteral("%${entry.key}%")
                    .replacement(entry.value)
                    .build()
            )
        }

        return comp
    }

    /**
     * Оборачивает компонент в компонент с явно отключенным курсивом.
     * Полезно, чтобы убирать курсив из описаний и названий предметов.
     * */

    override fun deitalize(comp: Component): Component {
        return Component.empty().decoration(TextDecoration.ITALIC, false).children(mutableListOf(comp))
    }

    override fun processAndDeitalize(text: String, lang: Locale?, vararg placeholders: LocalePairBase): Component {
        return deitalize(process(text, lang ?: data.defaultLang, *placeholders))
    }

    override fun process(text: String, lang: Locale?, replacing: Collection<LocalePairBase>): Component {
        return process(text, lang, *replacing.toTypedArray())
    }

    override fun process(text: String, lang: Locale?, vararg replacing: LocalePairBase): Component {
        val mapComps = mutableMapOf<String, Component>()
        val mapStrings = mutableMapOf<String, String>()

        for (pair in replacing) {
            when (pair) {
                is LocalePairBase.component -> mapComps[pair.key] = pair.value
                is LocalePairBase.string -> mapStrings[pair.key] = pair.value
            }
        }

        return _process(text, mapStrings, mapComps)
    }

    override fun process(text: String, vararg replacing: LocalePairBase): Component {
        return process(text = text, lang = data.defaultLang, *replacing)
    }
}