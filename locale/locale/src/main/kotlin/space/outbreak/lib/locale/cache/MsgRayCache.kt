package space.outbreak.lib.locale.cache

import com.google.common.cache.CacheBuilder
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import space.outbreak.lib.locale.LPB
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

internal object MsgRayCache {
    val counter = AtomicLong(0L)

    private val cache = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .build<MsgRayID, Component>()

    class TmpCacheEntry(
        val key: Key,
        val args: Array<out LPB>
    )

    private val tmpCache = CacheBuilder.newBuilder()
//        .expireAfterWrite(1, TimeUnit.MINUTES)
        .build<Long, TmpCacheEntry>();

    private fun newId(): Long {
        return counter.incrementAndGet()
    }

    fun addToTmp(key: Key, args: Array<out LPB>): Long {
        val id = newId()
        tmpCache.put(id, TmpCacheEntry(key, args))
        return id
    }

    fun getTmpAndInvalidate(id: Long): TmpCacheEntry? {
        val entry = tmpCache.getIfPresent(id) ?: return null
        tmpCache.invalidate(id)
        return entry
    }

    fun add(id: Long, lang: Locale, comp: Component) {
        cache.put(MsgRayID(id, lang), comp)
    }

    fun get(id: MsgRayID): Component? {
        return cache.getIfPresent(id)
    }

    fun clear() {
        cache.invalidateAll()
    }
}