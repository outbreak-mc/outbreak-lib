package space.outbreak.lib.v2.locale.source

interface ICustomColorTagsSource : ILocaleSource {
    fun getCustomColorTags(): Map<String, String>
}