package space.outbreak.lib.paperplugin

import space.outbreak.lib.api.locale.GlobalLocaleDataManager
import space.outbreak.lib.utils.locale.SealedLocaleBase

sealed class L : SealedLocaleBase({ GlobalLocaleDataManager.data("outbreaklib") }) {
    class LOADED__NS_FORMAT() : L()
    class LOADED__STATS(
        val `load-time`: Long,
        ns: Collection<String>,
        val `total-keys`: Int,
        val `total-placeholders`: Int,
        val `total-color-tags`: Int,
        nsFormat: String,
    ) : L() {
        val `namespaces` = ns.joinToString(", ") { nsFormat.replace("%namespace%", it) }
        val `total-namespaces`: Int = ns.size
        val `namespaces-size`: Int = ns.size
    }
}