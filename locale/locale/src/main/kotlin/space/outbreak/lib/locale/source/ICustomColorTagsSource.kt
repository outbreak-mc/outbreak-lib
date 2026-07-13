package space.outbreak.lib.locale.source

interface ICustomColorTagsSource : ILocaleSource {
    fun getCustomColorTags(): Map<String, String>
}