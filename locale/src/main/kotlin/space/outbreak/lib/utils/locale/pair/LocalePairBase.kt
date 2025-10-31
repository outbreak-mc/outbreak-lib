package space.outbreak.lib.utils.locale.pair

import net.kyori.adventure.text.Component

sealed class LocalePairBase {
    abstract val key: String
    abstract val value: Any

    data class string(
        override val key: String,
        override val value: String,
    ) : LocalePairBase()

    data class component(
        override val key: String,
        override val value: Component,
    ) : LocalePairBase()

//    data class locale(
//        override val key: String,
//        override val value: IL,
//    ) : LocalePairBase()
}

infix fun String.means(that: Component): LocalePairBase.component {
    return LocalePairBase.component(this, that)
}

infix fun String.means(that: Any): LocalePairBase.string {
    return LocalePairBase.string(this, that.toString())
}