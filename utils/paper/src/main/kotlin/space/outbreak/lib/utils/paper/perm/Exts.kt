package space.outbreak.lib.utils.paper.perm

import dev.jorel.commandapi.ExecutableCommand
import dev.jorel.commandapi.arguments.AbstractArgument
import org.bukkit.permissions.Permissible


inline fun <Impl : ExecutableCommand<Impl, CS>, CS> ExecutableCommand<Impl, CS>.withPermission(p: IPermEnum) {
    this.withPermission(p.node)
}

inline fun <
        T,
        Impl : AbstractArgument<T, Impl, A, CS>,
        A : AbstractArgument<*, *, A, CS>,
        CS
        > AbstractArgument<T, Impl, A, CS>.withPermission(p: IPermEnum): Impl {
    return this.withPermission(p.node)
}

inline fun Permissible.hasPermission(p: IPermEnum) = hasPermission(p.node)