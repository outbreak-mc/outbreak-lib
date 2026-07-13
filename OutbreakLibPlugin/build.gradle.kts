import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    alias(libs.plugins.shadow)
    alias(libs.plugins.pluginyml.paper)
    alias(libs.plugins.paperweight.userdev)
}

group = "${rootProject.group}.paperplugin"
version = rootProject.version

paper {
    name = "OutbreakLib"
    version = rootProject.version.toString()
    description = rootProject.description
    website = "https://outbreak.space"
    author = "OUTBREAK"
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP

    main = "${rootProject.group}.paperplugin.OutbreakLibPlugin"
    loader = "${rootProject.group}.utils.paper.Loader"

    hasOpenClassloader = true

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

    paperLibrary(rootProject.libs.jetbrains.exposed.core)
    paperLibrary(rootProject.libs.jetbrains.exposed.migration)
    paperLibrary(rootProject.libs.jetbrains.exposed.dao)
    paperLibrary(rootProject.libs.jetbrains.exposed.jdbc)

    paperLibrary(rootProject.libs.hikaricp)
    paperLibrary(rootProject.libs.caffeine)
    paperLibrary(rootProject.libs.semver)

    compileOnly(rootProject.libs.commandapi.core)
    compileOnly(rootProject.libs.commandapi.kotlin)

    paperLibrary(rootProject.libs.apache.commons.text)
}


kotlin {
    jvmToolchain(21)
}

tasks.shadowJar {
    // relocate("com.github.benmanes", "${rootProject.group}.shaded.benmanes")
    // relocate("org.semver4j", "${rootProject.group}.shaded.semver")

    exclude("/org/slf4j/")
    exclude("/org/jspecify/")

    mergeServiceFiles()
    manifest {
        attributes("Implementation-Version" to rootProject.version)
    }

    archiveFileName.set("OutbreakLib-${rootProject.version}.jar")
    
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
    dependsOn(tasks.reobfJar)
}