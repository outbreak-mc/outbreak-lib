package space.outbreak.lib.utils.locale

import net.kyori.adventure.key.Key
import net.kyori.adventure.translation.Translator
import java.util.*

interface ILocaleData {
    val formatter: Formatter
    val translator: Translator
    val namespaceKey: Key

    /** Возвращает [Set] имеющихся языков */
    val languages: Set<Locale>

    /** @return Язык по умолчанию. Стандартной имплементации возвращает первый язык из [languages] */
    val defaultLang: Locale get() = languages.first()

    fun removeLang(lang: Locale)
    fun addCustomColorTags(tags: Map<String, String>)
    fun String.toYamlStyleKey(): String
    fun String.isEnumStyleKey(): Boolean
    fun getRaw(lang: Locale, key: String): String?
    fun getKeys(lang: Locale, keyFormat: KeyStyle): Collection<TranslationKey>

    /**
     * Добавляет данные из [config] в языковой словарь
     *
     * @param lang название языка вида `ru_RU` (формат, который возвращается `Locale.toString()`).
     *
     * @param config карта вида {ключ: перевод}. Ключи ожидаются в формате путей из yaml:
     *  <br>
     *  `example.path.my-key`
     * */
    fun load(lang: Locale, config: Map<String, Any?>)

    /** Удаляет все загруженные словари, плейсхолдеры и прочие данные */
    fun clear()
    fun getCustomColorTags(): Map<String, String>
}