package space.outbreak.lib.locale

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import net.kyori.adventure.translation.GlobalTranslator
import net.kyori.adventure.translation.Translator
import java.util.*


@Suppress("MemberVisibilityCanBePrivate")
open class LocaleData(
    private val namespaceKey: Key
) {
    // Все возможные нэймспэйсы, имеющиеся в compiledTree для быстрого доступа
    private val _namespaces = mutableSetOf<String>()
    private val compiledTree: MutableMap<Locale, MutableMap<Key, String>> = mutableMapOf()
    private val customColorTags: MutableMap<String, String> = mutableMapOf()

    lateinit var serializer: MiniMessage
    lateinit var translator: Translator

    init {
        recalculateSerializer()
    }

    fun removeLang(lang: Locale) {
        compiledTree.remove(lang)
        _namespaces.clear()
        for ((_, v) in compiledTree)
            for ((k, _) in v)
                _namespaces.add(k.namespace())
    }

    val namespaces: Set<String>
        get() = _namespaces

    fun addCustomColorTags(tags: Map<String, String>) {
        customColorTags.putAll(tags)
        recalculateSerializer()
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

    fun raw(lang: Locale, key: Key): String {
        return compiledTree[lang]?.get(key) ?: key.toString()
    }

    fun raw(key: Key): String {
        return compiledTree[defaultLang]?.get(key) ?: key.toString()
    }

    fun rawOrNull(lang: Locale, key: Key): String? {
        return compiledTree[lang]?.get(key)
    }

//    fun rawOrNull(lang: Locale, key: Key, vararg replacing: LocalePairBase<*>): String? {
//        val str = rawOrNull(lang, key) ?: return null
//        val valueMap = replacing.associate { (k, v) ->
//            k to if (v is Component) {
//                serializer.serialize(v)
//            } else {
//                v.toString()
//            }
//        }
//        val ss = StringSubstitutor(valueMap, "<", ">", '\\')
//        return ss.replace(str)
//    }

    companion object {
//        private fun processArgs(vararg replacing: LocalePairBase<*>): Array<ComponentLike> {
//            return Array(replacing.size) { i ->
//                when (val el = replacing[i]) {
//                    is LocalePairBase.component -> Argument.component(el.key, el.value)
//                    is LocalePairBase.string -> Argument.string(el.key, el.value)
//                    is LocalePairBase.il -> Argument.component(el.key, el.value.comp())
//                }
//            }
//        }

        private fun replacingToPlaceholders(vararg replacing: LocalePairBase<*>): List<TagResolver.Single> {
            return replacing.map { (key, value) ->
                when (val v = value) {
                    is ComponentLike -> Placeholder.component(key, v)
                    else -> Placeholder.parsed(key, v.toString())
                }
            }
        }
    }

    fun getMiniMessage(): MiniMessage {
        return serializer
    }

    fun getKeys(lang: Locale): Collection<Key> {
        return (compiledTree[lang] ?: mapOf()).keys
    }

    private fun createSerializer(): MiniMessage {
        val customColorTagsResolvers = customColorTags.mapNotNull { (tag, colorValue) ->
            val color = TextColor.fromCSSHexString(colorValue) ?: return@mapNotNull null
            TagResolver.resolver(tag, Tag.styling(color))
        }

        return MiniMessage.builder()
            .tags(
                TagResolver.builder()
                    .resolvers(StandardTags.defaults())
                    .resolvers(*customColorTagsResolvers.toTypedArray())
                    .build()
            ).build()
    }

    fun recalculateSerializer() {
        if (this::translator.isInitialized)
            GlobalTranslator.translator().removeSource(translator)
        serializer = createSerializer()
        translator = OptimizedLocaleTranslator(namespaceKey, this, serializer)
        GlobalTranslator.translator().addSource(translator)
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
    private fun compileMap(namespace: String, map: Map<String, Any?>): MutableMap<Key, String> {
        val out = mutableMapOf<Key, String>()
        map.forEach { (rawK, v) ->
            val k = rawK.toYamlStyleKey()
            if (v !is Map<*, *>) {
                out[Key.key(namespace, k)] = v.toString()
            } else {
                @Suppress("UNCHECKED_CAST")
                compileMap(namespace, v as Map<String, Any>).forEach { (innerK, innerV) ->
                    out[Key.key(namespace, k + "." + innerK.value())] = innerV
                }
            }
        }
        return out
    }

    /**
     * Добавляет данные из [config] в языковой словарь
     *
     * @param namespace пространство имён для объектов [Key]
     * @param config карта вида {ключ: перевод}. Ключи ожидаются в виде объектов [Key], у которых
     *  в качестве значения - пути в yaml-стиле.
     * */
    fun load(lang: Locale, namespace: String, config: Map<String, Any?>) {
        val translations = compileMap(namespace, config)

        for ((k, _) in translations)
            _namespaces.add(k.namespace())

        compiledTree.getOrPut(lang) { mutableMapOf() }.putAll(translations)
    }

    init {
        recalculateSerializer()
    }

    /** Возвращает [Set] имеющихся языков */
    val languages: Set<Locale>
        get() = compiledTree.keys

    /**
     * Возвращает язык по умолчанию. Если язык всего один, всегда возвращает его.
     *
     * @throws IllegalArgumentException при попытке установки незагруженного языка
     * */
    var defaultLang: Locale = Locale.of("en", "US")
        get() {
            if (languages.size == 1 || (languages.isNotEmpty() && field !in languages)) return languages.first()
            return field
        }
        set(value) {
            if (value !in languages) {
                if (languages.isEmpty())
                    throw IllegalStateException("Unable to set default language to \"$value\": No languages loaded!")
                throw IllegalArgumentException(
                    "Unable to set default language: " +
                            "There is no language \"$value\" in loaded languages (\"" +
                            languages.joinToString("\", \"") { it.toString() }
                            + "\")!")
            }
            field = value
        }

    /** Удаляет все загруженные словари, плейсхолдеры и прочие данные */
    fun clear() {
        compiledTree.clear()
        customColorTags.clear()
        _namespaces.clear()
    }

    fun getCustomColorTags(): Map<String, String> {
        return customColorTags
    }
}

