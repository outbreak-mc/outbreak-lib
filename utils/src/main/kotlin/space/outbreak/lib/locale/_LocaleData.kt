package space.outbreak.lib.locale

import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags

@Suppress("MemberVisibilityCanBePrivate", "ClassName")
class _LocaleData {
    val compiledTree: MutableMap<LangKey, MutableMap<TranslationKey, String>> = mutableMapOf()
    val placeholdersLangSpecific: LangSpecificStaticPlaceholders = mutableMapOf()
    val placeholdersGlobal: StaticPlaceholders = mutableMapOf()
    val customColorTags: MutableMap<String, String> = mutableMapOf()

    internal lateinit var serializer: MiniMessage

    fun removeLang(lang: String) {
        compiledTree.remove(lang)
    }

    fun removeLangPlaceholders(lang: String) {
        placeholdersLangSpecific.remove(lang)
    }

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
     * Добавляет данные из [config] в языковой словарь
     *
     * @param lang название языка вида `ru_RU` (формат, который возвращается `Locale.toString()`).
     *
     * @param config карта вида {ключ: перевод}. Ключи ожидаются в формате путей из yaml:
     *  <br>
     *  `example.path.my-key`
     * */
    fun load(lang: String, config: Map<String, Any?>) {
        compiledTree.getOrPut(lang) { mutableMapOf() }.putAll(compileMap(config))
    }

    init {
        recalculateSerializer()
    }

    /** Возвращает [Set] имеющихся языков */
    val languages: Set<String>
        get() = compiledTree.keys

    /**
     * Возвращает язык по умолчанию. Если язык всего один, всегда возвращает его.
     * Не может быть пустой строкой. В случае присвоения пустой строки, становится `"default"`.
     *
     * @throws IllegalArgumentException при попытке установки незагруженного языка
     * */
    var defaultLang: String = "default"
        get() {
            if (field != "default" && field != "")
                return field
            else
                field = if (languages.isNotEmpty()) languages.first() else "default"
            return field
        }
        set(value) {
            val v = if (value == "") {
                "default"
            } else value

            if (v != "default" && !languages.contains(v)) {
                if (languages.isEmpty())
                    throw IllegalStateException("No languages loaded!")
                throw IllegalArgumentException("Language \"$v\" is not loaded!")
            }
            field = v
        }

    /** Удаляет все загруженные словари, плейсхолдеры и прочие данные */
    fun clear() {
        compiledTree.clear()
        placeholdersLangSpecific.clear()
        placeholdersGlobal.clear()
        customColorTags.clear()
        defaultLang = ""
    }
}
