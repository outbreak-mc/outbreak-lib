package space.outbreak.lib.utils.locale

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import space.outbreak.lib.utils.locale.pair.LocalePairBase

/** Объект для удобного хранения полей из [ILocaleEnum] с заранее заданными placeholder'ами */
class PreparedLocale(
    val locale: ILocaleEnum,
    val replacing: Array<out LocalePairBase>,
) {
    fun comp(lang: Locale? = null): Component {
        return locale.comp(lang = lang, *replacing)
    }

    // fun raw(lang: Locale? = null): String {
    //     return locale.raw(lang = lang, *replacing)
    // }

    fun send(audience: Audience) {
        locale.send(audience, *replacing)
    }

    fun sendActionBar(audience: Audience) {
        locale.sendActionBar(audience, *replacing)
    }
}