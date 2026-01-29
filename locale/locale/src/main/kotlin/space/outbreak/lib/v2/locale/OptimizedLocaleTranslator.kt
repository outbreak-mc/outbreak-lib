package space.outbreak.lib.v2.locale

import com.github.benmanes.caffeine.cache.Caffeine
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.translation.Translator
import java.text.MessageFormat
import java.util.*
import java.util.concurrent.TimeUnit

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

    // Система кэширования для избавления от лишних вызовов парсинга. По умолчанию,
    // каждое сообщение отправляется каждому из игроков с вызовом парсинга каждый раз. Данный
    // кэш призван сохранять перекодированное в компонент после первой отправки сообщение для
    // отправки остальным получателям.
    // Ключ (rayId: Long) генерируется в вызове tcomp(), то есть един в рамках одного заготовленного
    // сообщения, после чего данные кэшируются по мере рассылки.
    // Если вызвано tcomp(ray = -1), оптимизация не применяется.
    private val propagationCache = Caffeine.newBuilder()
        .expireAfterWrite(5, TimeUnit.SECONDS)
        .build<Long, Component>()

    override fun canTranslate(key: String, locale: Locale): Boolean {
        // У kyori очень странная и кринжовая система проверки. Если не переопределён метод canTranslate,
        // в его дефолтной реализации буквально вызывается translate() просто чтобы проверить, что
        // переводчик может такое перевести, а потом... Результат просто выкидывается и translate() потом
        // вызывают ещё раз, уже для финального возврата. Зачем вообще было делать отдельный метод для
        // проверки, если translate() всё равно может возвращать null, так что приходится осуществлять
        // двойную (или точнее x1.5) проверку... Так ещё и эта безумная дефолтнаяреализация с двойным
        // вызовом translate()... Я бы и не заметил этого кринжа, если бы не попытался реализовать
        // кэширование. Оказалось, что проверочный вызов translate() происходит без аргументов, из-за
        // чего в логах было видно двойной вызов translate(), а в кэш попадали отрендеренные строки,
        // но без подстановок. Как же чёрт возьми много времени я потратил, чтобы понять всё это...
        //
        // Делать нечего, придётся дважды вызывать split(). Ну, по крайней мере наличие в data.namespace
        // можно будет не проверять потом...
        val spl = key.split(':', limit = 3)
        return (spl.size == 3 && spl[1] in data.namespaces) || (spl.size == 2 && spl[0] in data.namespaces)
    }

    override fun translate(component: TranslatableComponent, locale: Locale): Component? {
        val spl = component.key().split(':', limit = 3)

        if (spl.size == 2)
            return mmTranslator.translate(component, locale)

        val (rayIdStr, namespace, key) = spl
        val realKey = "$namespace:$key"

        return propagationCache.get(rayIdStr.toLong()) {
            mmTranslator.translate(component.key(realKey), locale)!!
        }
    }
}