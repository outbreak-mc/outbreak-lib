package space.outbreak.lib.utils

data class PlaceholdersConfig(
    var `static-placeholders`: Map<String, String> = mapOf(),
    var `custom-color-tags`: Map<String, String> = mapOf(),
)