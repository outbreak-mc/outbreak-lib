package space.outbreak.lib.paperplugin

internal class DebugConfig(
    var `migrate-if-unstable`: Boolean = false
)

internal class Config(
    var `server-name`: String = "server",
    var debug: DebugConfig = DebugConfig(),
    var sources: List<Map<String, Any>> = listOf()
)