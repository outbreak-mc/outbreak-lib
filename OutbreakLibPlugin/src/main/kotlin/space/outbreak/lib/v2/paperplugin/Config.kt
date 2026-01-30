package space.outbreak.lib.v2.paperplugin

internal class DebugConfig(
    var `migrate-if-unstable`: Boolean = false
)

internal class Config(
    var `server-name`: String = "server",
    var debug: DebugConfig = DebugConfig(),
    var sources: List<Map<String, Any>> = listOf()
)