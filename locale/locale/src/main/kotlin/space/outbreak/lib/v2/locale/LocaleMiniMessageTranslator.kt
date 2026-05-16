package space.outbreak.lib.v2.locale

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

    override fun name() = key

    override fun canTranslate(key: String, locale: Locale): Boolean {
        return key.split(":", limit = 1)[0] in data.namespaces
    }

    override fun getMiniMessageString(key: String, locale: Locale): String? {
        val spl = key.split(':', limit = 2)
        val key = Key.key(spl[0], spl[1])

        val out = data.rawOrNull(locale, key)
        if (out != null) return out

        for (l in data.languages)
            return data.rawOrNull(l, key) ?: continue

        return out
    }
}