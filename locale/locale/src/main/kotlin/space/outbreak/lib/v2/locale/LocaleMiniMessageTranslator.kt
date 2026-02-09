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

    override fun name(): Key {
        return key
    }

    override fun getMiniMessageString(key: String, locale: Locale): String? {
        val locale = if (locale in data.languages) locale else data.defaultLang
        
        val spl = key.split(':', limit = 2)
        if (spl[0] !in data.namespaces)
            return null

        val key = Key.key(spl[0], spl[1])
        val out = data.rawOrNull(locale, key)

        if (out == null) {
            for (l in data.languages) {
                val t = data.rawOrNull(l, key)
                if (t != null)
                    return t
            }
        }

        return out
    }
}