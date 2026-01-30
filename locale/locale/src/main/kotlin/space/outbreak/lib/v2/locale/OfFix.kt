package space.outbreak.lib.v2.locale

import java.util.*

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