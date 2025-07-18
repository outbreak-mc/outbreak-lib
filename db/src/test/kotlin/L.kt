import space.outbreak.lib.locale.ILocaleEnum
import space.outbreak.lib.locale.LocaleData
import space.outbreak.lib.locale.LocaleDataManager


enum class L : ILocaleEnum {
    SIMPLEST,
    PATH__WITH__THREE__DOTS_AND_HYPHENS,
    MSG__WITH_PLACEHOLDERS,
    CUSTOM_COLOR_TAGS,
    ;

    override val data: LocaleData = LocaleDataManager.data("outbreaklib.test")
}