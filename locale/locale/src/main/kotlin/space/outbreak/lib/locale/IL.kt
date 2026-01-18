package space.outbreak.lib.locale

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.translation.GlobalTranslator
import space.outbreak.lib.locale.cache.MsgCache
import java.util.*

typealias LPB = LocalePairBase<*>

interface IL {
    companion object {
        fun replacingToPlaceholders(lang: Locale, vararg replacing: LPB): List<TagResolver.Single> {
            return replacing.map { (key, value) ->
                when (value) {
                    is IL -> Placeholder.component(key, value.comp(lang))
                    is TranslatableComponent -> Placeholder.component(key, GlobalTranslator.render(value, lang))
                    is ComponentLike -> Placeholder.component(key, value)
                    else -> Placeholder.parsed(key, value.toString())
                }
            }
        }

        fun replacingSanityCheck(key: String, vararg replacing: LPB) {
            val keys = mutableSetOf<String>()
            for ((k, v) in replacing) {
                if (k !in keys)
                    keys.add(k)
                else
                    throw IllegalStateException("${k} is already in keys for ${key}!")
            }
        }
    }

    fun getLocaleData(): LocaleData = GlobalLocaleData

    val langKey: Key

    fun raw(lang: Locale, vararg replacing: LPB): String {
        return getLocaleData().raw(lang, langKey, *replacing)
    }

    fun raw(vararg replacing: LPB): String {
        return getLocaleData().raw(langKey, *replacing)
    }

    fun rawOrNull(lang: Locale, vararg replacing: LocalePairBase<*>): String? {
        return getLocaleData().rawOrNull(lang, langKey, *replacing)
    }

    /** @return Переведённый на язык [lang] компонент.
     * Если перевода не найдено, возвращает компонент, содержащий ключ перевода в качестве текста. */
    fun comp(lang: Locale, vararg replacing: LPB): Component {
        replacingSanityCheck(langKey.toString(), *replacing)
        val mm = getLocaleData().getMiniMessage()
        return if (replacing.isEmpty())
            MsgCache.getOrPutToStaticCache(langKey.asString(), lang) { mm.deserialize(raw(lang)) }
        else
            mm.deserialize(raw(lang), *replacingToPlaceholders(lang, *replacing).toTypedArray())
    }

//    /** @return Переведённый на язык [lang] компонент. Если перевода не найдено, возвращает null. */
//    fun compOrNull(lang: Locale, vararg replacing: LPB): Component? {
//        val mm = getLocaleData().getMiniMessage()
//        val raw = rawOrNull(lang) ?: return null
//        return if (replacing.isEmpty()) {
//            MsgCache.getOrPutToStaticCache(langKey.asString(), lang) { mm.deserialize(raw) }
//        } else {
//            mm.deserialize(raw, *replacingToPlaceholders(lang, *replacing).toTypedArray())
//        }
//    }

//    /**
//     * Вызывается перед финальным созданием компонента, в том числе в вызовах через [tcomp],
//     * когда язык отправителя становится известен только в момент отправки, и позволяет
//     * изменить состояние объекта в этот момент.
//     * */
//    fun onCompute(lang: Locale, audience: Audience) {}

    fun tcomp(vararg replacing: LPB): TranslatableComponent {
        val id = MsgCache.addToTmp(langKey, this, replacing)
        return Component.translatable("$LIBCACHED_NS:$id")
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