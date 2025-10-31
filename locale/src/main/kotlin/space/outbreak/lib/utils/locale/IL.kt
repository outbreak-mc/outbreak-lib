package space.outbreak.lib.utils.locale

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.minimessage.translation.Argument
import space.outbreak.lib.utils.locale.pair.LocalePairBase
import java.util.*

interface IL {
    val data: LocaleData
    val name: String
    val key: Key

    fun raw(lang: Locale? = null): String {
        return data.getRaw(lang ?: data.defaultLang, key.asString()) ?: key.asString()
    }

    fun rawOrNull(lang: Locale?): String?

    /** @return Переведённый на язык [lang] компонент. Если перевода не найдено, возвращает ключ перевода. */
    fun comp(lang: Locale?, vararg replacing: LocalePairBase): Component {
        val tc = comp(*replacing)
        return data.translator.translate(tc, lang ?: data.defaultLang)
            ?: Component.text(tc.key())
    }

    private fun processArgs(vararg replacing: LocalePairBase): Array<ComponentLike> {
        return Array(replacing.size) { i ->
            when (val el = replacing[i]) {
                is LocalePairBase.component -> Argument.component(el.key, el.value)
                is LocalePairBase.string -> Argument.string(el.key, el.value)
            }
        }
    }

    fun comp(vararg replacing: LocalePairBase): TranslatableComponent {
        return Component.translatable(key.asString(), *processArgs(*replacing))
    }

    fun send(audience: Audience, vararg replacing: LocalePairBase) {
        audience.sendMessage(comp(*replacing))
    }

    fun send(audience: Audience, locale: Locale?, vararg replacing: LocalePairBase) {
        audience.sendMessage(comp(locale ?: data.defaultLang, *replacing))
    }

    fun sendActionBar(audience: Audience, locale: Locale?, vararg replacing: LocalePairBase) {
        audience.sendActionBar(comp(locale ?: data.defaultLang))
    }

    fun sendActionBar(audience: Audience, vararg replacing: LocalePairBase) {
        audience.sendActionBar(comp(*replacing))
    }
}