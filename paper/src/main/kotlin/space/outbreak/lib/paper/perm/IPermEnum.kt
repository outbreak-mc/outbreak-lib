package space.outbreak.lib.paper.perm

import org.bukkit.permissions.Permissible

interface IPermEnum {
    val root: String
    val node: String

    fun nameToNode() = "$root.${toString().replace("__", ".").replace("_", "-").lowercase()}"

    fun check(permissible: Permissible): Boolean {
        return permissible.hasPermission(node)
    }
}