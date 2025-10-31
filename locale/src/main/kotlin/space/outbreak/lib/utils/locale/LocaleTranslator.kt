package space.outbreak.lib.utils.locale

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslator
import java.util.*

class LocaleTranslator(
    private val key: Key,
    localeData: LocaleData
) : MiniMessageTranslator(localeData.serializer) {
    private val data: LocaleData = localeData

    override fun name(): Key {
        return key
    }

    override fun getMiniMessageString(key: String, locale: Locale): String? {
        return data.getRaw(locale, key)
    }
}