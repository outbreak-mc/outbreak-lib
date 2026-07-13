package space.outbreak.lib.utils.paper.perm

abstract class IPermEnum(rootNode: String) {
    open val root: String = rootNode

    final val node = "$root.${toString().replace("__", ".").replace("_", "-").lowercase()}"
}