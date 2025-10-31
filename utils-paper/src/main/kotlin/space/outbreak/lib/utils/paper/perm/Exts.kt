package space.outbreak.lib.utils.paper.perm

import dev.jorel.commandapi.ExecutableCommand
import org.bukkit.command.CommandSender
import org.bukkit.permissions.Permissible

inline fun Permissible.hasPermission(permission: IPermEnum): Boolean {
    return hasPermission(permission.node)
}

inline fun <Impl : ExecutableCommand<Impl, CommandSender>> ExecutableCommand<Impl, CommandSender>.withPermission(
    permission: IPermEnum,
): Impl {
    return withPermission(permission.node)
}