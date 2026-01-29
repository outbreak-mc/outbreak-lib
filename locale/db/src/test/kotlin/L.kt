import net.kyori.adventure.key.Key
import space.outbreak.lib.v2.locale.IL
import space.outbreak.lib.v2.locale.LocaleData


enum class L : IL {
    SIMPLEST,
    PATH__WITH__THREE__DOTS_AND_HYPHENS,
    MSG__WITH_PLACEHOLDERS,
    CUSTOM_COLOR_TAGS,
    ;

    override val langKey: Key = Key.key("outbreaklib", this.name)
    override val data: LocaleData = LocaleData(langKey)
}