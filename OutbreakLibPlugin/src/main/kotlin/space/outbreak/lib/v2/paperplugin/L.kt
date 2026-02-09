@file:Suppress("ClassName", "PropertyName", "RemoveRedundantBackticks")

package space.outbreak.lib.v2.paperplugin

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import space.outbreak.lib.v2.locale.GlobalLocaleData
import space.outbreak.lib.v2.locale.SealedLocaleBase

sealed class L(offset: Array<String> = arrayOf()) : SealedLocaleBase(
    { GlobalLocaleData },
    "outbreaklib",
    offsetNodes = offset
) {
    sealed class LOADED : L(arrayOf("loaded")) {
        class NS_FORMAT(val namespace: String) : LOADED()
        class NS_SEP : LOADED()

        protected fun joinNamespaces(ns: Iterable<String>) = Component.join(
            JoinConfiguration.separator(NS_SEP().tcomp()),
            ns.map { NS_FORMAT(it).tcomp() }
        )

        class STATS(val `load-time`: Long, ns: Collection<String>, val `total-keys`: Int, val `total-color-tags`: Int) :
            LOADED() {
            val `namespaces` = joinNamespaces(ns)
            val `total-namespaces`: Int = ns.size
        }

        class LOADED_MORE(val keys: Int, ns: Collection<String>) : LOADED() {
            val `namespaces` = joinNamespaces(ns)
        }
    }

    class BENCHMARK(
        val `batches`: Int,
        val `batch-size`: Int,
        val `messages`: Int,
        val `cached-time`: Long,
        val `direct-time`: Long,
    ) : L()
}
