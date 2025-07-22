package space.outbreak.lib.locale

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import space.outbreak.lib.locale.pair.LocalePairBase

interface ILocaleEnum {
    val name: String

    /**
     * Объект содержащий данные переводов для нужного нэймспэйса.
     * ```
     * override val data = LocaleDataManager.data("namespace")
     * ```
     * */
    val data: LocaleData

    fun comp(lang: String? = null, vararg replacing: LocalePairBase): Component {
        return data.formatter.process(raw(lang), lang, *replacing)
    }

    fun comp(vararg replacing: LocalePairBase): Component {
        return data.formatter.process(raw(null), null, *replacing)
    }

    fun compOrNull(lang: String? = null, vararg replacing: LocalePairBase): Component? {
        return data.formatter.process(rawOrNull(lang) ?: return null, lang, *replacing)
    }

    fun compOrNull(vararg replacing: LocalePairBase): Component? {
        return data.formatter.process(rawOrNull(null) ?: return null, null, *replacing)
    }

    fun rawOrNull(lang: String? = null, vararg replacing: LocalePairBase.StringPair): String? {
        return data.formatter.stringReplaceAll(
            data.formatter.byPath(lang = lang, path = name) ?: return null,
            *replacing
        )
    }

    fun rawOrNull(vararg replacing: LocalePairBase.StringPair): String? {
        return data.formatter.stringReplaceAll(
            data.formatter.byPath(lang = null, path = name) ?: return null,
            *replacing
        )
    }

    fun raw(lang: String? = null, vararg replacing: LocalePairBase.StringPair): String {
        return data.formatter.stringReplaceAll(
            data.formatter.byPath(lang = lang, path = name) ?: return name,
            *replacing
        )
    }

    fun raw(vararg replacing: LocalePairBase.StringPair): String {
        return data.formatter.stringReplaceAll(
            data.formatter.byPath(lang = null, path = name) ?: return name,
            *replacing
        )
    }

    fun send(audience: Audience, lang: String?, vararg replacing: LocalePairBase) {
        audience.sendMessage(comp(lang, *replacing))
    }

    fun send(audience: Audience, vararg replacing: LocalePairBase) {
        audience.sendMessage(comp(null, *replacing))
    }

    fun sendActionBar(audience: Audience, lang: String? = null, vararg replacing: LocalePairBase) {
        audience.sendActionBar(comp(lang, *replacing))
    }

    fun sendActionBar(audience: Audience, vararg replacing: LocalePairBase) {
        audience.sendActionBar(comp(null, *replacing))
    }

    fun use(vararg replacing: LocalePairBase): PreparedLocale {
        return PreparedLocale(this, replacing)
    }
}