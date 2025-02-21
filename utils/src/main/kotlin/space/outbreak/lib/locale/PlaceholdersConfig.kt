package space.outbreak.lib.locale

import com.fasterxml.jackson.annotation.JsonProperty

data class PlaceholdersConfig(
    @JsonProperty("static-placeholders")
    val staticPlaceholders: Map<String, String>,
    @JsonProperty("custom-color-tags")
    val customColorTags: Map<String, String>,
)
