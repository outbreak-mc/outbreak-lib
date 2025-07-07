package space.outbreak.lib.sealedlocale

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import space.outbreak.lib.locale.ILocaleEnum.Companion.byPath
import space.outbreak.lib.locale.ILocaleEnum.Companion.data
import space.outbreak.lib.locale.ILocaleEnum.Companion.process
import space.outbreak.lib.locale.ILocaleEnum.Companion.stringReplaceAll
import kotlin.reflect.KProperty

abstract class SealedLocaleBase(
    private vararg val offsetNodes: String,
) {
    constructor(offsetNodes: Collection<String>) : this(*offsetNodes.toTypedArray())

    private val name: String by lazy {
        if (offsetNodes.isEmpty())
            this::class.simpleName!!
        else
            offsetNodes.joinToString(separator = "__") { it.uppercase() } + "__" + this::class.simpleName!!
    }

    private val replacing: List<Pair<String, Any>> by lazy {
        this::class.members
            .filterIsInstance<KProperty<*>>()
            .filter { it.parameters.size == 1 }
            .mapNotNull { field ->
                val value = field.getter.call(this) ?: return@mapNotNull null
                field.name to value
            }
    }

    private val replacingMap by lazy {
        replacing.associate { it.first to it.second }
    }

    fun raw(lang: String?): String {
        return stringReplaceAll(byPath(lang = lang ?: data.defaultLang, path = name) ?: return name, replacingMap)
    }

    fun rawOrNull(lang: String?): String? {
        return stringReplaceAll(
            byPath(lang = lang ?: data.defaultLang, path = name) ?: return null,
            replacingMap
        )
    }

    fun comp(lang: String? = null): Component {
        return process(
            byPath(lang = lang ?: data.defaultLang, path = name) ?: return Component.text(name),
            lang,
            replacing
        )
    }

    fun send(audience: Audience, lang: String? = null) {
        audience.sendMessage(comp(lang))
    }

    fun sendActionBar(audience: Audience, lang: String? = null) {
        audience.sendActionBar(comp(lang))
    }
}