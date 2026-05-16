package space.outbreak.lib.v2.locale

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

infix fun String.means(that: Any): LPB {
    return when (that) {
        is Component -> Placeholder.component(this, that)
        else -> Placeholder.parsed(this, that.toString())
    }
}

infix fun String.meansUnparsed(that: Any): LPB {
    return Placeholder.unparsed(this, that.toString())
}

data class RawPair(
    val key: String,
    val value: String
)

infix fun String.meansRaw(that: Any): RawPair {
    return RawPair(this, that.toString())
}