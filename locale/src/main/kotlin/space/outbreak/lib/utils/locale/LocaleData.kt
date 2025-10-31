package space.outbreak.lib.utils.locale

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import net.kyori.adventure.translation.Translator
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
class LocaleData internal constructor(
    internal val compiledTree: MutableMap<Locale, MutableMap<TranslationKey, String>> = mutableMapOf(),
    internal val customColorTags: MutableMap<String, String> = mutableMapOf(),
    override val namespaceKey: Key
) : ILocaleData {
    override val formatter = Formatter(this)
    override val translator: Translator = LocaleTranslator(namespaceKey, this)

    internal lateinit var serializer: MiniMessage

    internal fun removeLangCompletely(lang: Locale) {
        compiledTree.remove(lang)
    }

    override fun removeLang(lang: Locale) {
        compiledTree.remove(lang)
    }

    override fun addCustomColorTags(tags: Map<String, String>) {
        customColorTags.putAll(tags)
        recalculateSerializer()
    }

    internal fun String.toEnumStyleKey(): String {
        return uppercase().replace("-", "_").replace(".", "__")
    }

    override fun String.toYamlStyleKey(): String {
        return lowercase().replace("__", ".").replace("_", "-")
    }

    override fun String.isEnumStyleKey(): Boolean {
        return (isNotBlank() && first().isUpperCase()) && contains("__") && !contains(".")
    }

    override fun getRaw(lang: Locale, key: String): String? {
        return if (!key.isEnumStyleKey()) {
            compiledTree[lang]?.get(key.toEnumStyleKey())
        } else {
            compiledTree[lang]?.get(key)
        }
    }

    override fun getKeys(lang: Locale, keyFormat: KeyStyle): Collection<TranslationKey> {
        val keys = (compiledTree[lang] ?: mapOf()).keys
        return when (keyFormat) {
            KeyStyle.YAML_PATH -> keys.map { it.toYamlStyleKey() }
            KeyStyle.ENUM_KEY -> keys
        }
    }

    internal fun recalculateSerializer() {
        val customColorTagsResolvers = customColorTags.mapNotNull { (tag, colorValue) ->
            val color = TextColor.fromCSSHexString(colorValue) ?: return@mapNotNull null
            TagResolver.resolver(tag, Tag.styling(color))
        }

        serializer = MiniMessage.builder()
            .tags(
                TagResolver.builder()
                    .resolvers(StandardTags.defaults())
                    .resolvers(*customColorTagsResolvers.toTypedArray())
                    .build()
            ).build()
    }

    /**
     * Переводит данные из прочитанного файла в единую карту строк без вложенных карт с
     * ключами в скомпилированном формате, где поля вложенных структур разделяются `__`, `-` заменяются на `_`,
     * и все ключи переводятся в верхний регистр.
     * Пример:
     * ```yaml
     * reloaded: "Конфигурация перезагружена"
     * err:
     *  perm: "У вас недостаточно прав, чтобы выполнять эту команду"
     *  player-not-found: "Игрок не найден"
     * ```
     * Будет преобразовано в
     * ```kotlin
     * mutableMapOf<String, String>(
     *   "RELOADED" to "Конфигурация перезагружена"
     *   "ERR__PERM" to "У вас недостаточно прав, чтобы выполнять эту команду"
     *   "ERR__PLAYER_NOT_FOUND" to "Игрок не найден"
     * )
     * ```
     * */
    private fun compileMap(map: Map<String, Any?>): MutableMap<String, String> {
        val out = mutableMapOf<String, String>()
        map.forEach { (rawK, v) ->
            val k = rawK.toEnumStyleKey()
            if (v !is Map<*, *>) {
                out[k] = v.toString()
            } else {
                @Suppress("UNCHECKED_CAST")
                compileMap(v as Map<String, Any>).forEach { (innerK, innerV) ->
                    out["${k}__${innerK}".uppercase()] = innerV
                }
            }
        }
        return out
    }

    /**
     * Добавляет данные из [config] в языковой словарь
     *
     * @param config карта вида {ключ: перевод}. Ключи ожидаются в формате путей из yaml:
     *  <br>
     *  `example.path.my-key`
     * */
    override fun load(lang: Locale, config: Map<String, Any?>) {
        val translations = compileMap(config)
        compiledTree.getOrPut(lang) { mutableMapOf() }.putAll(translations)
    }

    init {
        recalculateSerializer()
    }

    /** Возвращает [Set] имеющихся языков */
    override val languages: Set<Locale>
        get() = compiledTree.keys

//    /**
//     * Возвращает язык по умолчанию. Если язык всего один, всегда возвращает его.
//     * Не может быть пустой строкой. В случае присвоения пустой строки, становится `"default"`.
//     *
//     * @throws IllegalArgumentException при попытке установки незагруженного языка
//     * */
//    internal var defaultLang: String = "default"
//        get() {
//            if (field != "default" && field != "")
//                return field
//            else
//                field = if (languages.isNotEmpty()) languages.first() else "default"
//            return field
//        }
//        set(value) {
//            val v = if (value == "") {
//                "default"
//            } else value
//
//            if (v != "default" && !languages.contains(v)) {
//                if (languages.isEmpty())
//                    throw IllegalStateException("No languages loaded!")
//                throw IllegalArgumentException("Language \"$v\" is not loaded!")
//            }
//            field = v
//        }

    /** Удаляет все загруженные словари, плейсхолдеры и прочие данные */
    override fun clear() {
        compiledTree.clear()
        customColorTags.clear()
    }

    override fun getCustomColorTags(): Map<String, String> {
        return customColorTags
    }
}
