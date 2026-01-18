package space.outbreak.lib.locale

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import java.util.*

typealias LPB = LocalePairBase<*>

interface IL {
    fun getLocaleData(): LocaleData = GlobalLocaleData

    val langKey: Key

    fun raw(lang: Locale?, vararg replacing: LocalePairBase<*>): String {
        return getLocaleData().raw(lang ?: getLocaleData().defaultLang, langKey, *replacing)
    }

    fun rawOrNull(lang: Locale, vararg replacing: LocalePairBase<*>): String? {
        return getLocaleData().rawOrNull(lang, langKey, *replacing)
    }

    /** @return Переведённый на язык [lang] компонент. Если перевода не найдено, возвращает ключ перевода. */
    fun comp(lang: Locale, vararg replacing: LPB): Component {
        val tc = tcomp(*replacing)
        return getLocaleData().translator.translate(tc, lang) ?: Component.text(tc.key())
    }

//    /**
//     * Вызывается перед финальным созданием компонента, в том числе в вызовах через [tcomp],
//     * когда язык отправителя становится известен только в момент отправки, и позволяет
//     * изменить состояние объекта в этот момент.
//     * */
//    fun onCompute(lang: Locale, audience: Audience) {}

    fun tcomp(vararg replacing: LPB): TranslatableComponent {
        return getLocaleData().tcomp(langKey, *replacing)
    }

    fun send(audience: Audience, vararg replacing: LPB) {
        audience.sendMessage(tcomp(*replacing))
    }

    fun send(audience: Audience, lang: Locale, vararg replacing: LPB) {
        audience.sendMessage(comp(lang, *replacing))
    }

    fun sendActionBar(audience: Audience, lang: Locale, vararg replacing: LPB) {
        audience.sendActionBar(comp(lang, *replacing))
    }

    fun sendActionBar(audience: Audience, vararg replacing: LPB) {
        audience.sendActionBar(tcomp(*replacing))
    }
}