package space.outbreak.lib.locale

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import space.outbreak.lib.locale.pair.LocalePairBase
import space.outbreak.lib.locale.pair.means
import kotlin.reflect.KProperty

abstract class SealedLocaleBase(
    private val data: LocaleData,
    private vararg val offsetNodes: String,
) {
    constructor(data: LocaleData, offsetNodes: Collection<String>) : this(data, *offsetNodes.toTypedArray())

    private val name: String by lazy {
        if (offsetNodes.isEmpty())
            this::class.simpleName!!
        else
            offsetNodes.joinToString(separator = "__") { it.uppercase() } + "__" + this::class.simpleName!!
    }

    private val replacing: Array<LocalePairBase> by lazy {
        this::class.members
            .filterIsInstance<KProperty<*>>()
            .filter { it.parameters.size == 1 }
            .mapNotNull { field ->
                val value = field.getter.call(this) ?: return@mapNotNull null
                field.name means value
            }.toTypedArray()
    }
    //
    // private val replacingMap by lazy {
    //     replacing.associate { it.first to it.second }
    // }

    fun raw(lang: String?): String {
        return data.formatter.stringReplaceAll(
            data.formatter.byPath(lang = lang ?: data.defaultLang, path = name) ?: return name, *replacing
        )
    }

    fun rawOrNull(lang: String?): String? {
        return data.formatter.stringReplaceAll(
            data.formatter.byPath(lang = lang ?: data.defaultLang, path = name) ?: return null,
            *replacing
        )
    }

    fun comp(lang: String? = null): Component {
        return data.formatter.process(
            data.formatter.byPath(lang = lang ?: data.defaultLang, path = name) ?: return Component.text(name),
            lang,
            *replacing
        )
    }

    fun send(audience: Audience, lang: String? = null) {
        audience.sendMessage(comp(lang))
    }

    fun sendActionBar(audience: Audience, lang: String? = null) {
        audience.sendActionBar(comp(lang))
    }
}