package space.outbreak.lib.locale

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslator
import java.util.*

internal class LocaleMiniMessageTranslator(
    private val key: Key,
    localeData: LocaleData,
    miniMessage: MiniMessage
) : MiniMessageTranslator(miniMessage) {
    private val data: LocaleData = localeData

    override fun name(): Key {
        return key
    }

    override fun getMiniMessageString(key: String, locale: Locale): String? {
        val spl = key.split(':', limit = 2)
        if (spl[0] !in data.namespaces)
            return null
        return data.raw(locale, Key.key(spl[0], spl[1]))
    }
}