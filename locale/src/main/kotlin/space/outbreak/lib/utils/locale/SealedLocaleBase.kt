package space.outbreak.lib.utils.locale

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import space.outbreak.lib.utils.locale.pair.LocalePairBase
import space.outbreak.lib.utils.locale.pair.means
import kotlin.reflect.KProperty

abstract class SealedLocaleBase(
    private val dataGetter: () -> LocaleData,
    private vararg val offsetNodes: String,
) : IL {
    constructor(dataGetter: () -> LocaleData, offsetNodes: Collection<String>) : this(
        dataGetter,
        *offsetNodes.toTypedArray()
    )

    override val data get() = dataGetter()

    override val name: String by lazy {
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

    override fun raw(lang: Locale?): String {
        return data.formatter.stringReplaceAll(
            data.formatter.byPath(lang = lang ?: data.defaultLang, path = name) ?: return name, *replacing
        )
    }

    override fun rawOrNull(lang: Locale?): String? {
        return data.formatter.stringReplaceAll(
            data.formatter.byPath(lang = lang ?: data.defaultLang, path = name) ?: return null,
            *replacing
        )
    }

    override fun comp(lang: Locale?): Component {
        return data.formatter.process(
            data.formatter.byPath(lang = lang ?: data.defaultLang, path = name) ?: return Component.text(name),
            lang,
            *replacing
        )
    }

    override fun send(audience: Audience, lang: Locale?) {
        audience.sendMessage(comp(lang))
    }

    override fun sendActionBar(audience: Audience, lang: Locale?) {
        audience.sendActionBar(comp(lang))
    }
}