import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    alias(libs.plugins.shadow)
    alias(libs.plugins.pluginyml.paper)
    alias(libs.plugins.paperweight.userdev)
}

group = "space.outbreak.lib.paperplugin"
version = rootProject.version

paper {
    name = rootProject.name
    version = rootProject.version.toString()
    description = rootProject.description
    website = "https://outbreak.space"
    author = "OUTBREAK"
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP

    main = "space.outbreak.lib.paperplugin.OutbreakLibPlugin"
    loader = "space.outbreak.lib.paper.shaded.Loader"

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
    paperweight.paperDevBundle(rootProject.libs.versions.paper.version)

    implementation(project(":db"))
    implementation(project(":db-shaded-utils"))
    implementation(project(":utils"))
    implementation(project(":paper"))
    implementation(project(":paper-shaded"))

    implementation(rootProject.libs.jetbrains.exposed.core)
    implementation(rootProject.libs.jetbrains.exposed.migration)
    implementation(rootProject.libs.jetbrains.exposed.dao)
    implementation(rootProject.libs.jetbrains.exposed.jdbc)
    implementation(rootProject.libs.hikaricp)

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
    exclude("/kotlin/")
    exclude("/kotlinx/")

    mergeServiceFiles()
    manifest {
        attributes("Implementation-Version" to rootProject.version)
    }

    archiveFileName.set("${rootProject.name}Plugin-${rootProject.version}.jar")
    // destinationDirectory.set(file("D:\\test_server\\plugins\\"))
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
    dependsOn(tasks.reobfJar)
}