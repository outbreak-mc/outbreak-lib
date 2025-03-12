package space.outbreak.lib.locale

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.format.TextDecoration
import org.apache.commons.text.StringSubstitutor


interface ILocaleEnum {
    val name: String

    /**
     * Оборачивает компонент в компонент с явно отключенным курсивом.
     * Полезно, чтобы убирать курсив из описаний и названий предметов.
     * */
    fun deitalize(comp: Component): Component {
        return Component.empty().decoration(TextDecoration.ITALIC, false).children(mutableListOf(comp))
    }

    fun processAndDeitalize(text: String, lang: String?, vararg placeholders: Pair<String, Any>): Component {
        return deitalize(process(text, lang ?: data.defaultLang, *placeholders))
    }

    fun processAndDeitalize(text: String, vararg placeholders: Pair<String, Any>): Component {
        return deitalize(process(text, data.defaultLang, *placeholders))
    }

    fun comp(lang: String?, vararg replacing: Pair<String, Any>): Component {
        return process(rawOrNull(lang) ?: raw(data.defaultLang), lang, *replacing)
    }

    fun comp(vararg replacing: Pair<String, Any>): Component {
        return process(raw(data.defaultLang), data.defaultLang, *replacing)
    }

    fun compOrNull(lang: String?, vararg replacing: Pair<String, Any>): Component? {
        return process(rawOrNull(lang) ?: return null, lang, *replacing)
    }

    fun compOrNull(vararg replacing: Pair<String, Any>): Component? {
        return process(rawOrNull(data.defaultLang) ?: return null, data.defaultLang, *replacing)
    }

    private fun byPath(path: String, lang: String?): String? {
        return data.compiledTree[lang ?: data.defaultLang]?.get(path)
    }

    fun rawOrNull(lang: String?, vararg replacing: Pair<String, Any>): String? {
        return stringReplaceAll(byPath(lang = lang ?: data.defaultLang, path = name) ?: return null, mapOf(*replacing))
    }

    fun rawOrNull(vararg replacing: Pair<String, Any>): String? {
        return stringReplaceAll(byPath(lang = data.defaultLang, path = name) ?: return null, mapOf(*replacing))
    }

    fun raw(lang: String?, vararg replacing: Pair<String, Any>): String {
        return stringReplaceAll(byPath(lang = lang ?: data.defaultLang, path = name) ?: return name, mapOf(*replacing))
    }

    fun raw(vararg replacing: Pair<String, Any>): String {
        return stringReplaceAll(byPath(lang = data.defaultLang, path = name) ?: return name, mapOf(*replacing))
    }

    fun send(audience: Audience, vararg replacing: Pair<String, Any>) {
        audience.sendMessage(comp(null, *replacing))
    }

    fun sendActionBar(audience: Audience, vararg replacing: Pair<String, Any>) {
        audience.sendActionBar(comp(null, *replacing))
    }

    companion object {
        /**
         * Внутреннее поле, необходимое для работы объектов. Должно быть определено в enum следующим образом:
         * ```
         * override val data: LocaleData = LocaleData()
         * ```
         * */
        internal inline val data: _LocaleData
            get() = LocaleData.data

        /**
         * Подставляет плейсхолдеры из карты [placeholders]` в строку [str],
         * возвращает получившуюся строку.
         *
         * Производительнее, чем просто `replace()` и поддерживает экранирование
         * символа `%` при помощи `\`.
         * */
        @JvmStatic
        fun stringReplaceAll(str: String, placeholders: Map<String, Any?>): String {
            val substitutor = StringSubstitutor(placeholders, "%", "%", '\\')
            return substitutor.replace(str)
        }

        @JvmStatic
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

        @JvmStatic
        fun process(text: String, lang: String?, vararg replacing: Pair<String, Any>): Component {
            val mapComps = mutableMapOf<String, Component>()
            val mapStrings = data.placeholdersGlobal.toMutableMap()
            data.placeholders[lang ?: data.defaultLang]?.let { mapStrings.putAll(it) }

            for (pair in replacing) {
                if (pair.second is Component)
                    mapComps[pair.first] = pair.second as Component
                else
                    mapStrings[pair.first] = pair.second.toString()
            }

            return _process(text, mapStrings, mapComps)
        }

        @JvmStatic
        fun process(text: String, vararg replacing: Pair<String, Any>): Component {
            return process(text = text, lang = data.defaultLang, *replacing)
        }
    }
}