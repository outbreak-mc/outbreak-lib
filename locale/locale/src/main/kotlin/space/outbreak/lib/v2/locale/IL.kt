package space.outbreak.lib.v2.locale

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.translation.Argument
import org.apache.commons.text.StringSubstitutor
import space.outbreak.lib.v2.locale.cache.MsgCache
import java.util.*

typealias LPB = TagResolver.Single
typealias LPR = RawPair

interface IL {
    fun preprocessReplacing(vararg replacing: LPB): Array<out LPB> = replacing

    fun getLocaleData(): LocaleData = GlobalLocaleData

    val translationKey: Key

    fun raw(lang: Locale, vararg replacing: LPR): String {
        return StringSubstitutor(replacing.associate { it.key to it.value }, "<", ">", '\\')
            .replace(getLocaleData().raw(lang, translationKey))
    }

    fun rawOrNull(lang: Locale, vararg replacing: LPR): String? {
        val str = getLocaleData().rawOrNull(lang, translationKey) ?: return null
        return StringSubstitutor(replacing.associate { it.key to it.value }, "<", ">", '\\')
            .replace(str)
    }

    /** @return Переведённый на язык [lang] компонент.
     * Если перевода не найдено, возвращает компонент, содержащий ключ перевода в качестве текста. */
    fun comp(lang: Locale, vararg replacing: LPB): Component {
        val mm = getLocaleData().getMiniMessage()
        val replacing = preprocessReplacing(*replacing)

        return if (replacing.isEmpty())
            MsgCache.getOrPutToStaticCache(translationKey.asString(), lang) { mm.deserialize(raw(lang)) }
        else {
            mm.deserialize(getLocaleData().raw(lang, translationKey), *replacing)
        }
    }

    fun tcomp(vararg replacing: LPB): TranslatableComponent {
        val ray = MsgCache.newRay()
        return Component.translatable(
            "${ray}:${translationKey.asString()}",
            *preprocessReplacing(*replacing).map { Argument.tagResolver(it) }.toTypedArray()
        )
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