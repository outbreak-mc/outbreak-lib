package space.outbreak.lib.utils.locale

data class PlaceholdersConfig(
    var `static-placeholders`: Map<String, String> = mapOf(),
    var `custom-color-tags`: Map<String, String> = mapOf(),
)
