package space.outbreak.lib.locale

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.translation.Translator
import space.outbreak.lib.locale.cache.MsgRayCache
import space.outbreak.lib.locale.cache.MsgRayID
import java.text.MessageFormat
import java.util.*

internal class OptimizedLocaleTranslator(
    private val translatorName: Key,
    localeData: LocaleData,
    miniMessage: MiniMessage
) : Translator {
    private val data: LocaleData = localeData
    private val mmTranslator = LocaleMiniMessageTranslator(
        translatorName, data, miniMessage
    )

    override fun name(): Key {
        return translatorName
    }

    override fun translate(key: String, locale: Locale): MessageFormat? {
        return null
    }

    override fun translate(component: TranslatableComponent, locale: Locale): Component? {
        val spl = component.key().split(':')
        if (spl.size != 2)
            return null

        val (namespace, key) = spl
        if (namespace == LIBCACHED_NS) {
            val id = key.toLong()
            val cached = MsgRayCache.get(MsgRayID(id, locale))
            if (cached != null)
                return cached

            val cacheEntry = MsgRayCache.getTmpAndInvalidate(id)
                ?: throw IllegalArgumentException("Can't retrieve translatable component data from cache.")

            val comp = data.comp(locale, cacheEntry.key, *cacheEntry.args)
            MsgRayCache.add(id, locale, comp)
            return comp
        } else {
            return mmTranslator.translate(component, locale)
        }
    }
}