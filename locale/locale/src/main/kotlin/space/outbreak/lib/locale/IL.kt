package space.outbreak.lib.locale

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.translation.Argument
import org.apache.commons.text.StringSubstitutor
import space.outbreak.lib.locale.cache.MsgCache
import java.util.*

typealias LPB = LocalePairBase<*>

interface IL {
    companion object {
        fun replacingToPlaceholders(
            lang: Locale,
            vararg replacing: LPB
        ): List<TagResolver.Single> {
            return replacing.map { (key, value) ->
                when (value) {
                    is IL -> Placeholder.component(key, value.comp(lang))
                    is ComponentLike -> Placeholder.component(key, value)
                    else -> Placeholder.parsed(key, value.toString())
                }
            }
        }

        fun replacingToArguments(localeData: LocaleData, vararg replacing: LPB): List<ComponentLike> {
            return replacing.map { (key, value) ->
                when (value) {
                    is IL -> Argument.component(key, value.tcomp())
                    is ComponentLike -> Argument.component(key, value)
                    else -> Argument.component(key, localeData.serializer.deserialize(value.toString()))
//                    else -> Argument.string(key, value.toString())
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

        private fun processRaw(localeData: LocaleData, raw: String, vararg replacing: LPB): String {
            val valueMap = replacing.associate { (k, v) ->
                k to if (v is Component) {
                    localeData.serializer.serialize(v)
                } else {
                    v.toString()
                }
            }
            val ss = StringSubstitutor(valueMap, "<", ">", '\\')
            return ss.replace(raw)
        }
    }

    fun getLocaleData(): LocaleData = GlobalLocaleData

    val langKey: Key

    fun raw(lang: Locale, vararg replacing: LPB): String {
        return processRaw(getLocaleData(), getLocaleData().raw(lang, langKey), *replacing)
    }

    fun raw(vararg replacing: LPB): String {
        return getLocaleData().raw(langKey)
    }

    fun rawOrNull(lang: Locale, vararg replacing: LocalePairBase<*>): String? {
        return processRaw(getLocaleData(), getLocaleData().rawOrNull(lang, langKey) ?: return null, *replacing)
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

    fun tcomp(vararg replacing: LPB): TranslatableComponent {
//        val id = MsgCache.addToTmp(langKey, this)
        return Component.translatable(langKey.asString(), replacingToArguments(getLocaleData(), *replacing))
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