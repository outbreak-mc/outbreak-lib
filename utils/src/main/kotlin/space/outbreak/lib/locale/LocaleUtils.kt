package space.outbreak.lib.locale

import net.kyori.adventure.text.logger.slf4j.ComponentLogger


fun ComponentLogger.info(msg: ILocaleEnum, vararg replacing: Pair<String, Any>) {
    info(msg.comp(null, *replacing))
}

fun ComponentLogger.debug(msg: ILocaleEnum, vararg replacing: Pair<String, Any>) {
    debug(msg.comp(null, *replacing))
}

fun ComponentLogger.trace(msg: ILocaleEnum, vararg replacing: Pair<String, Any>) {
    trace(msg.comp(null, *replacing))
}

fun ComponentLogger.warn(msg: ILocaleEnum, vararg replacing: Pair<String, Any>) {
    warn(msg.comp(null, *replacing))
}

fun ComponentLogger.error(msg: ILocaleEnum, vararg replacing: Pair<String, Any>) {
    error(msg.comp(null, *replacing))
}