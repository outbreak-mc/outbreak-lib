package space.outbreak.lib.paperplugin

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.*
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import space.outbreak.lib.locale.KeyFormat
import space.outbreak.lib.locale.LocaleDataManager

class LocaleDebugCommand(
    private val plugin: OutbreakLibPlugin,
) {
    private val namespaceArgument = StringArgument("namespace")
        .replaceSuggestions(ArgumentSuggestions.stringCollection { info ->
            LocaleDataManager.namespaces
        })

    private val langArg = StringArgument("lang")
        .replaceSuggestions(ArgumentSuggestions.stringCollection { info ->
            val ns = info.previousArgs["namespace"] as String
            LocaleDataManager.data(ns).languages
        })

    private val keyArg = StringArgument("key")
        .replaceSuggestions(ArgumentSuggestions.stringCollection { info ->
            val lang = info.previousArgs["lang"] as String
            val ns = info.previousArgs["namespace"] as String
            LocaleDataManager.data(ns).getKeys(lang, KeyFormat.YAML_PATH_STYLE)
        })

    fun register() {
        commandTree("outbreaklib") {
            literalArgument("locale") {
                literalArgument("stats") {
                    playerExecutor { player, _ -> plugin.printStats().forEach { player.sendMessage(it) } }
                    consoleExecutor { _, _ -> plugin.printStats().forEach { plugin.componentLogger.info(it) } }
                }
                literalArgument("reload") {
                    anyExecutor { sender, _ ->
                        plugin.reload()
                        playerExecutor { player, _ -> plugin.printStats().forEach { player.sendMessage(it) } }
                        consoleExecutor { _, _ -> plugin.printStats().forEach { plugin.componentLogger.info(it) } }
                    }
                }
                literalArgument("checkkey") {
                    argument(namespaceArgument) {
                        argument(langArg) {
                            argument(keyArg) {
                                anyExecutor { sender, args ->
                                    val namespace: String by args
                                    val lang: String by args
                                    val key: String by args

                                    val raw = LocaleDataManager.data(namespace).getRaw(lang = lang, key = key)
                                    if (raw == null) {
                                        sender.sendMessage("'${key}' key is null")
                                    } else {
                                        sender.sendMessage(miniMessage().deserialize(raw))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun unregister() {
        CommandAPI.unregister("outbreaklib", true)
    }
}