package space.outbreak.lib.paperplugin

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import space.outbreak.lib.locale.GlobalLocaleData
import space.outbreak.lib.locale.SealedLocaleBase
import space.outbreak.lib.locale.means

sealed class L : SealedLocaleBase({ GlobalLocaleData }, "outbreaklib") {
    object LOADED__NS_FORMAT : L()

    class LOADED__STATS(
        val `load-time`: Long,
        ns: Collection<String>,
        val `total-keys`: Int,
//        val `total-placeholders`: Int,
        val `total-color-tags`: Int,
    ) : L() {
        //        val `namespaces` = miniMessage().deserialize(ns.joinToString(", ") { nsFormat.replace("<namespace>", it) })
        private val _nsComps = ns.map { LOADED__NS_FORMAT.comp("namespace" means it) }

        val `namespaces` = Component.join(
            JoinConfiguration.separator(Component.text(",")),
            _nsComps
        )
        val `total-namespaces`: Int = ns.size
        val `namespaces-size`: Int = ns.size
    }
}
