package space.outbreak.lib.locale.cache

import com.google.common.cache.CacheBuilder
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import space.outbreak.lib.locale.IL
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

/**
 * Система кэширования компонентов для избегания бесполезного повторного парсинга
 * при отправке одинаковых сообщений (и особенно при рассылке [TranslatableComponent])
 * */
object MsgCache {
    val counter = AtomicLong(0L)

    private val propagationCache = CacheBuilder.newBuilder()
        .expireAfterWrite(5, TimeUnit.SECONDS)
        .build<MsgRayID, Component>()

    private data class StaticCacheKey(val key: String, val lang: Locale)
    class TmpCacheEntry(val key: Key, val il: IL)

    // Кэш для сообщений, которые не имеют аргументов и парсить их каждый раз - бесполезно.
    private val staticCache = mutableMapOf<StaticCacheKey, Component>()

    // Очередь, куда попадают данные (IL, replacing) в вызовах tcomp(), чтобы потом достать их
    // в переводчике, ибо других способов передать какие-либо данные в переводчик нет.
    private val tmpCache = mutableMapOf<Long, TmpCacheEntry>()

    private fun newId(): Long {
        return counter.incrementAndGet()
    }

    /** Добавляет сообщение, в кэш для сообщений, которые не должны
     *  иметь аргументов и потому не требуют пере-парсинга */
    fun getOrPutToStaticCache(key: String, lang: Locale, defaultValue: () -> Component): Component {
        return staticCache.getOrPut(StaticCacheKey(key, lang), defaultValue)
    }

    fun addToTmp(key: Key, il: IL): Long {
        val id = newId()
        tmpCache[id] = TmpCacheEntry(key, il)
        return id
    }

    fun getTmpAndRemove(id: Long): TmpCacheEntry? {
        return tmpCache.remove(id)
    }

    fun add(id: Long, lang: Locale, comp: Component) {
        propagationCache.put(MsgRayID(id, lang), comp)
    }

    fun get(id: MsgRayID): Component? {
        return propagationCache.getIfPresent(id)
    }

    fun clear() {
        propagationCache.invalidateAll()
        tmpCache.clear()
        staticCache.clear()
    }
}