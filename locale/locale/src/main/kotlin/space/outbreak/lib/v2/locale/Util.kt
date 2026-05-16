package space.outbreak.lib.v2.locale

internal fun String.toEnumStyleKey(): String {
    return uppercase().replace("-", "_").replace(".", "__")
}

internal fun String.toYamlStyleKey(): String {
    return lowercase().replace("__", ".").replace("_", "-")
}

internal fun String.isEnumStyleKey(): Boolean {
    return (isNotBlank() && first().isUpperCase()) && contains("__") && !contains(".")
}