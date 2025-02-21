package space.outbreak.lib.locale

import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags

@Suppress("MemberVisibilityCanBePrivate", "ClassName")
internal class _LocaleData {
    internal val compiledTree: MutableMap<String, MutableMap<String, String>> = mutableMapOf()
    internal val placeholders: LangSpecificStaticPlaceholders = mutableMapOf()
    internal val placeholdersGlobal: StaticPlaceholders = mutableMapOf()
    internal val customColorTags: MutableMap<String, String> = mutableMapOf()

    internal lateinit var serializer: MiniMessage

    fun recalculateSerializer() {
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
            val k = rawK.replace("-", "_").uppercase()
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
     * Загружает языковой файл языка [lang] в словарь.
     * */
    fun load(lang: String, config: Map<String, Any?>) {
        compiledTree[lang] = compileMap(config)
    }

    init {
        recalculateSerializer()
    }

    /** Возвращает [Set] имеющихся языков */
    val languages: Set<String?>
        get() = compiledTree.keys

    /**
     * Возвращает язык по умолчанию. Если язык всего один, всегда возвращает его.
     * @throws IllegalArgumentException при попытке установки незагруженного языка
     * */
    var defaultLang: String = ""
        get() {
            if (field != "")
                return field
            else
                field = languages.first()!!
            return field
        }
        set(value) {
            if (value != "" && !languages.contains(value)) {
                if (languages.isEmpty())
                    throw IllegalStateException("No languages loaded!")
                throw IllegalArgumentException("Language \"$value\" is not loaded!")
            }
            field = value
        }

    /** Удаляет все загруженные словари, плейсхолдеры и прочие данные */
    fun clear() {
        compiledTree.clear()
        placeholders.clear()
        placeholdersGlobal.clear()
        customColorTags.clear()
        defaultLang = ""
    }
}
