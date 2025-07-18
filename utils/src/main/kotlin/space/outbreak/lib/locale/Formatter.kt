package space.outbreak.lib.locale

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.format.TextDecoration
import org.apache.commons.text.StringSubstitutor

class Formatter(val data: LocaleData) {
    /**
     * Подставляет плейсхолдеры из карты [placeholders]` в строку [str],
     * возвращает получившуюся строку.
     *
     * Производительнее, чем просто `replace()` и поддерживает экранирование
     * символа `%` при помощи `\`.
     * */
    fun stringReplaceAll(str: String, placeholders: Map<String, Any?>): String {
        val substitutor = StringSubstitutor(placeholders, "%", "%", '\\')
        return substitutor.replace(str)
    }

    fun byPath(path: String, lang: String?): String? {
        return data.compiledTree[lang ?: data.defaultLang]?.get(path)
    }

    fun _process(
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

    fun deitalize(comp: Component): Component {
        return Component.empty().decoration(TextDecoration.ITALIC, false).children(mutableListOf(comp))
    }

    fun processAndDeitalize(text: String, lang: String? = null, vararg placeholders: Pair<String, Any>): Component {
        return deitalize(process(text, lang ?: data.defaultLang, *placeholders))
    }

    fun process(text: String, lang: String?, vararg replacing: Pair<String, Any>): Component {
        val mapComps = mutableMapOf<String, Component>()
        val mapStrings = data.placeholdersGlobal.toMutableMap()
        data.placeholdersLangSpecific[lang ?: data.defaultLang]?.let { mapStrings.putAll(it) }

        for (pair in replacing) {
            if (pair.second is Component)
                mapComps[pair.first] = pair.second as Component
            else
                mapStrings[pair.first] = pair.second.toString()
        }

        return _process(text, mapStrings, mapComps)
    }

    fun process(text: String, lang: String?, replacing: Collection<Pair<String, Any>>): Component {
        val mapComps = mutableMapOf<String, Component>()
        val mapStrings = data.placeholdersGlobal.toMutableMap()
        data.placeholdersLangSpecific[lang ?: data.defaultLang]?.let { mapStrings.putAll(it) }

        for (pair in replacing) {
            if (pair.second is Component)
                mapComps[pair.first] = pair.second as Component
            else
                mapStrings[pair.first] = pair.second.toString()
        }

        return _process(text, mapStrings, mapComps)
    }

    fun process(text: String, vararg replacing: Pair<String, Any>): Component {
        return process(text = text, lang = data.defaultLang, *replacing)
    }
}