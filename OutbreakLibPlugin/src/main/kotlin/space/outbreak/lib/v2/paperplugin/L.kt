package space.outbreak.lib.v2.paperplugin

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import space.outbreak.lib.v2.locale.SealedLocaleBase

sealed class L : SealedLocaleBase(
    { _root_ide_package_.space.outbreak.lib.v2.locale.GlobalLocaleData },
    "outbreaklib"
) {
    class LOADED__NS_FORMAT(val namespace: String) : L()
    class LOADED__NS_SEP : L()

    class LOADED__STATS(
        val `load-time`: Long,
        ns: Collection<String>,
        val `total-keys`: Int,
//        val `total-placeholders`: Int,
        val `total-color-tags`: Int,
    ) : L() {
        //        val `namespaces` = miniMessage().deserialize(ns.joinToString(", ") { nsFormat.replace("<namespace>", it) })
        private val _nsComps = ns.map { LOADED__NS_FORMAT(it).tcomp() }

        val `namespaces` = Component.join(
            JoinConfiguration.separator(LOADED__NS_SEP().tcomp()),
            _nsComps
        )
        val `total-namespaces`: Int = ns.size
        val `namespaces-size`: Int = ns.size
    }

    class BENCHMARK(
        val `batches`: Int,
        val `batch-size`: Int,
        val messages: Int,
        val `cached-time`: Long,
        val `direct-time`: Long,
    ) : L()
}
