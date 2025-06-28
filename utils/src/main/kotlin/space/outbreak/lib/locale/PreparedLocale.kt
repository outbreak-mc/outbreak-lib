package space.outbreak.lib.locale

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component

/** Объект для удобного хранения полей из [ILocaleEnum] с заранее заданными placeholder'ами */
class PreparedLocale(
    val locale: ILocaleEnum,
    val replacing: Array<out Pair<String, Any>>,
) {
    fun comp(lang: String? = null): Component {
        return locale.comp(lang = lang, *replacing)
    }

    fun raw(lang: String? = null): String {
        return locale.raw(lang = lang, *replacing)
    }

    fun send(audience: Audience) {
        locale.send(audience, *replacing)
    }

    fun sendActionBar(audience: Audience) {
        locale.sendActionBar(audience, *replacing)
    }
}