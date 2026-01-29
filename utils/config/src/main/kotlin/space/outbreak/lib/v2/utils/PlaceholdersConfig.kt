package space.outbreak.lib.v2.utils

data class PlaceholdersConfig(
    var `static-placeholders`: Map<String, String> = mapOf(),
    var `custom-color-tags`: Map<String, String> = mapOf(),
)