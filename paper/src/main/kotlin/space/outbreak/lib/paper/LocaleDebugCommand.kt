package space.outbreak.lib.paper

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.*
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import space.outbreak.lib.locale.LocaleData

class LocaleDebugCommand(private val name: String) {
    private val langArg = StringArgument("lang")
        .replaceSuggestions(ArgumentSuggestions.stringCollection {
            LocaleData.getLanguages()
        })

    private val keyArg = StringArgument("key")
        .replaceSuggestions(ArgumentSuggestions.stringCollection { info ->
            val lang = info.previousArgs["lang"] as String
            LocaleData.getKeys(lang, true)
        })

    fun register() {
        commandTree(name) {
            literalArgument("checkkey") {
                argument(langArg) {
                    argument(keyArg) {
                        anyExecutor { sender, args ->
                            // val namespace: String by args
                            val lang: String by args
                            val key: String by args

                            val raw = LocaleData.getRaw(lang = lang, key = key)
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

    fun unregister() {
        CommandAPI.unregister(name)
    }
}