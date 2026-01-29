package space.outbreak.lib.v2.locale.cache

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import java.util.*
import java.util.concurrent.atomic.AtomicLong

/**
 * Система кэширования компонентов для избегания бесполезного повторного парсинга
 * при отправке одинаковых сообщений (и особенно при рассылке [TranslatableComponent])
 * */
object MsgCache {
    val counter = AtomicLong(0L)

    private data class PropCacheKey(
        val lang: String,
        val key: String
    )

    private data class StaticCacheKey(val key: String, val lang: Locale)

    // Кэш для сообщений, которые не имеют аргументов и парсить их каждый раз - бесполезно.
    private val staticCache = mutableMapOf<StaticCacheKey, Component>()

    fun newId(): Long {
        return counter.incrementAndGet()
    }

    /** Добавляет сообщение, в кэш для сообщений, которые не должны
     *  иметь аргументов и потому не требуют пере-парсинга */
    fun getOrPutToStaticCache(key: String, lang: Locale, defaultValue: () -> Component): Component {
        return staticCache.getOrPut(StaticCacheKey(key, lang), defaultValue)
    }

    fun clear() {
        staticCache.clear()
    }
}