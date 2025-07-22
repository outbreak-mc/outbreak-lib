package space.outbreak.lib.locale.pair

import net.kyori.adventure.text.Component

sealed class LocalePairBase {
    abstract val key: String
    abstract val value: Any

    data class StringPair(
        override val key: String,
        override val value: String,
    ) : LocalePairBase()

    data class CompPair(
        override val key: String,
        override val value: Component,
    ) : LocalePairBase()
}

infix fun String.means(that: Component): LocalePairBase.CompPair {
    return LocalePairBase.CompPair(this, that)
}

infix fun String.means(that: Any): LocalePairBase.StringPair {
    return LocalePairBase.StringPair(this, that.toString())
}