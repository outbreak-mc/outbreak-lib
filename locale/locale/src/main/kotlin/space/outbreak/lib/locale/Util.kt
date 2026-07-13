package space.outbreak.lib.locale

import java.util.*

val localeNameRegex = """^[a-z]{2}_[A-Z]{2}$""".toRegex()

fun ofExactLocaleOrNull(name: String): Locale? {
    if (!name.matches(localeNameRegex))
        return null
    val (lang, country) = name.split("_")
    return Locale.of(lang, country)
}

/** Парсит строку вида "en_US" правильным способом, без потери регистра (в отличие от [Locale.of]) */
fun ofExactLocale(str: String): Locale {
    if (str.length == 5) {
        val spl = str.split('_', limit = 2)
        if (spl.size != 2)
            return Locale.of(str)
        return Locale.of(spl[0], spl[1])
    }
    return Locale.of(str)
}

internal fun String.toEnumStyleKey(): String {
    return uppercase().replace("-", "_").replace(".", "__")
}

internal fun String.toYamlStyleKey(): String {
    return lowercase().replace("__", ".").replace("_", "-")
}

internal fun String.isEnumStyleKey(): Boolean {
    return (isNotBlank() && first().isUpperCase()) && contains("__") && !contains(".")
}