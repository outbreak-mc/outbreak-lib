package space.outbreak.lib.locale

import net.kyori.adventure.key.Key
import kotlin.reflect.KProperty

abstract class SealedLocaleBase(
    private val namespace: String,
    private vararg val offsetNodes: String
) : IL {
    open val data = GlobalLocaleData

    override val translationKey: Key by lazy {
        val classNameYamlStyle = this::class.simpleName!!.lowercase()
            .replace("__", ".")
            .replace("_", "-")

        val value = if (offsetNodes.isEmpty())
            classNameYamlStyle
        else
            offsetNodes.joinToString(separator = ".") { it.lowercase() } + "." + classNameYamlStyle
        Key.key(namespace, value)
    }

    private val replacing: Array<LPB> by lazy {
        this::class.members
            .filterIsInstance<KProperty<*>>()
            .filter { it.parameters.size == 1 && it.name != (::translationKey.name) && !it.name.startsWith("_") }
            .mapNotNull { field ->
                val value = field.getter.call(this) ?: return@mapNotNull null
                field.name means value
            }.toTypedArray()
    }

    override fun preprocessReplacing(vararg replacing: LPB): Array<out LPB> {
        return this.replacing + replacing
    }
}