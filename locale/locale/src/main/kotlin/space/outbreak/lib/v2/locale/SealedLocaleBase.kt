package space.outbreak.lib.v2.locale

import net.kyori.adventure.key.Key
import kotlin.reflect.KProperty

abstract class SealedLocaleBase(
    private val dataGetter: () -> LocaleData,
    private val namespace: String,
    private vararg val offsetNodes: String
) : IL {
    private val data get() = dataGetter()

    override val langKey: Key by lazy {
        val value = if (offsetNodes.isEmpty())
            this::class.simpleName!!.lowercase().replace("__", ".").replace("_", "-")
        else
            offsetNodes.joinToString(separator = ".") { it.lowercase() } + "." + this::class.simpleName!!
        Key.key(namespace, value)
    }

    private val replacing: Array<LocalePairBase<*>> by lazy {
        this::class.members
            .filterIsInstance<KProperty<*>>()
            .filter { it.parameters.size == 1 && it.name != "langKey" && !it.name.startsWith("_") }
            .mapNotNull { field ->
                val value = field.getter.call(this) ?: return@mapNotNull null
                field.name means value
            }.toTypedArray()
    }

    override fun preprocessReplacing(vararg replacing: LPB): Array<out LPB> {
        return this.replacing + replacing
    }
}