package space.outbreak.lib.locale

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.translation.Translator
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
//        val spl = component.key().split(':')
//        if (spl.size != 2)
//            return null
//
//        val (namespace, key) = spl
//        if (namespace == LIBCACHED_NS) {
//            // Если мы получили такой нэймспэйс, значит была использована система кэширования. После
//            // этого нэймспэйса вместо оригинального ключа подставлен фейковый, сгенерированный id для кэша.
//            // И тут два пути:
//            // 1. Это первый из вызовов, которые происходят для каждого получателя сообщения, и
//            //    тогда Component ещё не закэширован, но IL находится очереди (tmpCache). Нужно достать
//            //    его оттуда, отрендерить в нормальный Component, закэшировать и в отправить.
//            //
//            // 2. Это вызов уже для N-го игрока и тогда в кэше по такому id уже есть компонент,
//            //    тогда просто отправляем его.
//
//            // Если уже есть, отправляем
//            val id = key.toLong()
//            val cached = MsgCache.get(MsgRayID(id, locale))
//            if (cached != null)
//                return cached
//
//            // Если нет, рендерим. Отсутствие в очереди - повод для паники.
//            val cacheEntry = MsgCache.getTmpAndRemove(id)
//                ?: throw IllegalArgumentException("Can't retrieve translatable component data from cache.")
//
//            val actualComponentToTranslate = component.key(cacheEntry.key.asString())
//
//            val comp = (mmTranslator.translate(actualComponentToTranslate, locale) ?: return null)
//                .append(component.children())
//
//            MsgCache.add(id, locale, comp)
//            return comp
//        } else {
        println("translating ${component.key()}")
        return mmTranslator.translate(component, locale)
//        }
    }
}