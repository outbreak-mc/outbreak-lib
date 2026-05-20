package space.outbreak.lib.v2.locale

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import net.kyori.adventure.translation.GlobalTranslator
import net.kyori.adventure.translation.Translator
import space.outbreak.lib.v2.locale.source.ICustomColorTagsSource
import space.outbreak.lib.v2.locale.source.ILocaleSource
import space.outbreak.lib.v2.locale.source.ITranslationsSource
import java.util.*


@Suppress("MemberVisibilityCanBePrivate")
open class LocaleData(
    private val namespaceKey: Key,
    serverName: String
) {
    var serverName = serverName

    // Все возможные нэймспэйсы, имеющиеся в compiledTree для быстрого доступа
    private val _colorTags = mutableMapOf<String, String>()
    private val _namespaces = mutableSetOf<String>()
    private val compiledTree: MutableMap<Locale, MutableMap<Key, String>> = mutableMapOf()

    // private val staticPlaceholdersGlobal: MutableMap<String, MutableMap<String, String>> = mutableMapOf()
    // private val staticPlaceholders: MutableMap<String, MutableMap<Locale, MutableMap<String, String>>> = mutableMapOf()

    lateinit var serializer: MiniMessage
    lateinit var translator: Translator

    private val sources = mutableMapOf<Key, ILocaleSource>()

    fun addSource(source: ILocaleSource) {
        if (source.key in sources)
            throw IllegalStateException("Source with key ${source.key} already exists. Sources should be removed before re-adding.")
        sources[source.key] = source
    }

    fun removeSource(vararg keys: Key) {
        for (k in keys)
            sources.remove(k)
    }

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

    val namespaces: Set<String> get() = _namespaces

    fun process(str: String, vararg replacing: LPB): Component {
        return serializer.deserialize(str, *replacing)
    }

    fun rawOrNull(lang: Locale, key: Key): String? {
        return compiledTree[lang]?.get(key)
    }

    fun raw(lang: Locale, key: Key) = rawOrNull(lang, key) ?: key.toString()

    fun getMiniMessage(): MiniMessage {
        return serializer
    }

    fun getKeys(lang: Locale): Collection<Key> {
        return (compiledTree[lang] ?: mapOf()).keys
    }

    private fun createSerializer(): MiniMessage {
        val customColorTagsResolvers = _colorTags.mapNotNull { (tag, colorValue) ->
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

    private class LoadedStats(
        val keys: Int,
        val namespaces: Set<String>
    )

    private fun loadFromSource(source: ILocaleSource): LoadedStats {
        var keys = 0
        val namespaces = mutableSetOf<String>()

        if (source is ITranslationsSource) {
            val translations = source.getAllTranslations(serverName = serverName)
            for ((locale, map) in translations) {
                compiledTree.getOrPut(locale) { mutableMapOf() }.putAll(map)
                keys += map.size
                for ((key, _) in map) {
                    namespaces.add(key.namespace())
                    _namespaces.add(key.namespace())
                }
            }
        }

        if (source is ICustomColorTagsSource)
            _colorTags.putAll(source.getCustomColorTags())

        recalculateSerializer()

        return LoadedStats(keys, namespaces)
    }

    /** Загружает из добавленных источников все переводы и прочее,
     * предварительно очищая уже загруженные ранее данные. */
    fun loadAll() {
        clearData()

        for ((_, source) in sources) {
            source.init()
            loadFromSource(source)
        }
    }

    fun getColorTags(): Map<String, String> {
        return _colorTags
    }

    /** Добавляет источник и загружает из него данные */
    fun addAndLoad(source: ILocaleSource) {
        addSource(source)
        loadFromSource(source)
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
    fun clearData() {
        _colorTags.clear()
        compiledTree.clear()
        _namespaces.clear()
    }

    /** Заставляет забыть все подключенные источники данных */
    fun clearSources() {
        sources.clear()
    }
}

