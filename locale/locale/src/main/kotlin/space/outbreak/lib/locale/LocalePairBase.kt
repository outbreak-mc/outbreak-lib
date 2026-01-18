package space.outbreak.lib.locale

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage

sealed class LocalePairBase<V> {
    abstract val key: String
    abstract val value: V
    abstract val valueAsString: String

    class string(
        override val key: String,
        override val value: String,
    ) : LocalePairBase<String>() {
        override val valueAsString: String get() = value
    }

    class component(
        override val key: String,
        override val value: Component,
    ) : LocalePairBase<Component>() {
        override val valueAsString: String by lazy {
            miniMessage().serialize(value)
        }
    }

    operator fun component1(): String {
        return key
    }

    operator fun component2(): V {
        return value
    }
}

infix fun String.means(that: Any): LocalePairBase<*> {
    if (that is Component)
        return LocalePairBase.component(this, that)
    return LocalePairBase.string(this, that.toString())
}