package space.outbreak.lib.locale

import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import space.outbreak.lib.locale.pair.LocalePairBase


fun ComponentLogger.info(msg: ILocaleEnum, vararg replacing: LocalePairBase) {
    info(msg.comp(null, *replacing))
}

fun ComponentLogger.debug(msg: ILocaleEnum, vararg replacing: LocalePairBase) {
    debug(msg.comp(null, *replacing))
}

fun ComponentLogger.trace(msg: ILocaleEnum, vararg replacing: LocalePairBase) {
    trace(msg.comp(null, *replacing))
}

fun ComponentLogger.warn(msg: ILocaleEnum, vararg replacing: LocalePairBase) {
    warn(msg.comp(null, *replacing))
}

fun ComponentLogger.error(msg: ILocaleEnum, vararg replacing: LocalePairBase) {
    error(msg.comp(null, *replacing))
}