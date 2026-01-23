package space.outbreak.lib.paperplugin

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.*
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player
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
            withPermission(P.OUTBREAKLIB_USE)

            literalArgument("locale") {
                withPermission(P.LOCALE_RELOAD)
                literalArgument("stats") {
                    playerExecutor { player, _ -> plugin.printStats().send(player) }
                    consoleExecutor { _, _ -> plugin.printStats().let { plugin.componentLogger.info(it.tcomp()) } }
                }

                literalArgument("reload") {
                    anyExecutor { executor, _ ->
                        plugin.reload()
                        val stats = plugin.printStats()
                        if (executor is Player)
                            stats.send(executor)
                        plugin.componentLogger.info(stats.tcomp())
                    }
                }

                literalArgument("benchmark") {
                    integerArgument("chunksSize", 10, optional = true) {
                        integerArgument("chunksCount", 1, optional = true) {
                            booleanArgument("sendMessages", optional = true) {
                                anyExecutor { sender, args ->
                                    val chunksSize: Int? by args
                                    val chunksCount: Int? by args
                                    val sendMessages: Boolean? by args
                                    plugin.benchmark(
                                        chunksCount = chunksCount ?: 10,
                                        chunksSize = chunksSize ?: 10,
                                        audience = if (sendMessages ?: false) sender else null
                                    ).also { msg ->
                                        if (sender is Player)
                                            Bukkit.getConsoleSender().sendMessage(msg.tcomp())
                                        sender.sendMessage(msg.tcomp())
                                    }
                                }
                            }
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