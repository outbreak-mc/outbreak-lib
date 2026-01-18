package space.outbreak.lib.paperplugin

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.*
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import space.outbreak.lib.locale.GlobalLocaleData
import space.outbreak.lib.locale.LocaleData
import space.outbreak.lib.locale.ofExactLocale

class LocaleDebugCommand(
    private val plugin: OutbreakLibPlugin,
    private val ld: LocaleData
) {
    private val namespaceArgument = StringArgument("namespace")
        .replaceSuggestions(ArgumentSuggestions.stringCollection { info ->
            GlobalLocaleData.namespaces
        })

    private val langArg = StringArgument("lang")
        .replaceSuggestions(ArgumentSuggestions.stringCollection { info ->
//            val ns = info.previousArgs["namespace"] as String
            GlobalLocaleData.languages.map { it.toString() }
        })

    private val keyArg = StringArgument("key")
        .replaceSuggestions(ArgumentSuggestions.stringCollection { info ->
            val lang = info.previousArgs["lang"] as String
            val ns = info.previousArgs["namespace"] as String
            GlobalLocaleData.getKeys(ofExactLocale(lang)).map { it.value() }
        })

    fun register() {
        commandTree("outbreaklib") {
            literalArgument("locale") {
                literalArgument("stats") {
                    playerExecutor { player, _ -> plugin.printStats(ld).send(player) }
                    consoleExecutor { _, _ -> plugin.printStats(ld).let { plugin.componentLogger.info(it.comp()) } }
                }
                literalArgument("reload") {
                    anyExecutor { sender, _ ->
                        playerExecutor { player, _ ->
                            plugin.reload()
                            plugin.printStats(ld).send(player)
                        }
                        consoleExecutor { _, _ ->
                            plugin.reload()
                            plugin.printStats(ld).let { plugin.componentLogger.info(it.comp()) }
                        }
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

                                    val raw = ld.rawOrNull(
                                        lang = ofExactLocale(lang),
                                        key = Key.key(namespace, key)
                                    )
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