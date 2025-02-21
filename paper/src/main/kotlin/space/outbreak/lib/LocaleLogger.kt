package space.outbreak.lib
//
// import net.kyori.adventure.text.logger.slf4j.ComponentLogger
//
// class LocaleLogger(name: String) {
//     private val logger = ComponentLogger.logger(name)
//
//     fun info(msg: ILocaleEnum, vararg replacing: Pair<String, Any>) {
//         logger.info(msg.comp(null, *replacing))
//     }
//
//     fun info(msg: ILocaleEnum, lang: String, vararg replacing: Pair<String, Any>) {
//         logger.info(msg.comp(lang, *replacing))
//     }
//
//     fun debug(msg: ILocaleEnum, vararg replacing: Pair<String, Any>) {
//         logger.debug(msg.comp(null, *replacing))
//     }
//
//     fun debug(msg: ILocaleEnum, lang: String, vararg replacing: Pair<String, Any>) {
//         logger.debug(msg.comp(lang, *replacing))
//     }
//
//     fun warn(msg: ILocaleEnum, vararg replacing: Pair<String, Any>) {
//         logger.warn(msg.comp(null, *replacing))
//     }
//
//     fun warn(msg: ILocaleEnum, lang: String, vararg replacing: Pair<String, Any>) {
//         logger.warn(msg.comp(lang, *replacing))
//     }
//
//     fun error(msg: ILocaleEnum, vararg replacing: Pair<String, Any>) {
//         logger.error(msg.comp(null, *replacing))
//     }
//
//     fun error(msg: ILocaleEnum, lang: String, vararg replacing: Pair<String, Any>) {
//         logger.error(msg.comp(lang, *replacing))
//     }
//
//     fun trace(msg: ILocaleEnum, vararg replacing: Pair<String, Any>) {
//         logger.trace(msg.comp(null, *replacing))
//     }
//
//     fun trace(msg: ILocaleEnum, lang: String, vararg replacing: Pair<String, Any>) {
//         logger.trace(msg.comp(lang, *replacing))
//     }
// }