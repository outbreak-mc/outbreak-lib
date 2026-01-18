package space.outbreak.lib.locale

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import java.util.*
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


    private fun replacingWithAdditions(vararg addition: LocalePairBase<*>): Array<LocalePairBase<*>> {
        return replacing + addition
    }

    override fun comp(lang: Locale, vararg replacing: LPB): Component {
        return super.comp(lang, *replacingWithAdditions(*replacing))
    }

    fun comp(vararg replacing: LPB): Component {
        return super.comp(data.defaultLang, *replacingWithAdditions(*replacing))
    }

    override fun raw(lang: Locale?, vararg replacing: LocalePairBase<*>): String {
        return super.raw(lang, *replacingWithAdditions(*replacing))
    }

    override fun rawOrNull(lang: Locale, vararg replacing: LocalePairBase<*>): String? {
        return super.rawOrNull(lang, *replacingWithAdditions(*replacing))
    }

    override fun tcomp(vararg replacing: LPB): TranslatableComponent {
        return super.tcomp(*replacingWithAdditions(*replacing))
    }
}