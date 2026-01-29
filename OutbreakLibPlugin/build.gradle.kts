import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    alias(libs.plugins.shadow)
    alias(libs.plugins.pluginyml.paper)
    alias(libs.plugins.paperweight.userdev)
}

group = "${rootProject.group}.paperplugin"
version = rootProject.version

paper {
    name = rootProject.name
    version = rootProject.version.toString()
    description = rootProject.description
    website = "https://outbreak.space"
    author = "OUTBREAK"
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP

    main = "${rootProject.group}.paperplugin.OutbreakLibPlugin"
    loader = "${rootProject.group}.utils.paper.Loader"

    hasOpenClassloader = false

    generateLibrariesJson = true
    foliaSupported = true

    apiVersion = "1.21"

    serverDependencies {
        register("CommandAPI") {
            load = net.minecrell.pluginyml.paper.PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = true
        }
    }
}

dependencies {
    paperLibrary(kotlin("stdlib"))
    paperLibrary(kotlin("reflect"))
    paperweight.paperDevBundle(rootProject.libs.versions.paper.get())

    implementation(project(":utils"))
    implementation(project(":utils:db"))
    implementation(project(":utils:paper"))
    implementation(project(":utils:config"))
    implementation(project(":resapi"))
    implementation(project(":locale:locale"))
    implementation(project(":locale:db"))
    implementation(project(":locale:paper"))

    implementation(rootProject.libs.jetbrains.exposed.core)
    implementation(rootProject.libs.jetbrains.exposed.migration)
    implementation(rootProject.libs.jetbrains.exposed.dao)
    implementation(rootProject.libs.jetbrains.exposed.jdbc)
    implementation(rootProject.libs.hikaricp)
    implementation(rootProject.libs.caffeine)
    implementation(rootProject.libs.semver)

    compileOnly(rootProject.libs.commandapi.core)
    compileOnly(rootProject.libs.commandapi.kotlin)

    paperLibrary(rootProject.libs.apache.commons.text)
}


kotlin {
    jvmToolchain(21)
}

tasks.shadowJar {
    // relocate("com.fasterxml.jackson", "${rootProject.group}.shaded.com.fasterxml.jackson")
    relocate("org.jetbrains.exposed", "${rootProject.group}.shaded.exposed")
    relocate("com.zaxxer", "${rootProject.group}.shaded.hikaricp")
    relocate("com.github.ben-manes.caffeine", "${rootProject.group}.shaded.caffeine")
    relocate("org.semver4j:semver4j", "${rootProject.group}.shaded.semver")
    exclude("/kotlin/")
    exclude("/kotlinx/")

    mergeServiceFiles()
    manifest {
        attributes("Implementation-Version" to rootProject.version)
    }

    archiveFileName.set("${rootProject.name}Plugin-${rootProject.version}.jar")
    destinationDirectory.set(file("/home/shiny/OUTBREAK/test_server/plugins/"))
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
    dependsOn(tasks.reobfJar)
}