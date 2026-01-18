package space.outbreak.lib.locale

import net.kyori.adventure.text.Component

sealed class LocalePairBase<V> {
    abstract val key: String
    abstract val value: V
//    abstract val valueAsString: String

    class string(
        override val key: String,
        override val value: String,
    ) : LocalePairBase<String>()

    class component(
        override val key: String,
        override val value: Component,
    ) : LocalePairBase<Component>()

    class il(
        override val key: String,
        override val value: IL,
    ) : LocalePairBase<IL>()

    operator fun component1(): String {
        return key
    }

    operator fun component2(): V {
        return value
    }
}

infix fun String.means(that: Any): LocalePairBase<*> {
    return when (that) {
        is Component -> LocalePairBase.component(this, that)
        is IL -> LocalePairBase.il(this, that)
        else -> LocalePairBase.string(this, that.toString())
    }
}