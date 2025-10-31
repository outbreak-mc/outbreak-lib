import space.outbreak.lib.utils.locale.LocaleData
import space.outbreak.lib.utils.locale.LocaleDataManagerBase


enum class L : ILocaleEnum {
    SIMPLEST,
    PATH__WITH__THREE__DOTS_AND_HYPHENS,
    MSG__WITH_PLACEHOLDERS,
    CUSTOM_COLOR_TAGS,
    ;

    override val data: LocaleData = LocaleDataManagerBase.data("outbreaklib.test")
}