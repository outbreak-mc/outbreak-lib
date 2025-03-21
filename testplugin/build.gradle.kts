import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
    alias(libs.plugins.pluginyml.bukkit)
    alias(libs.plugins.paperweight.userdev)
}

group = "${rootProject.group}.plugin"

bukkit {
    version = rootProject.version.toString()
    name = "OutbreakLib"
    main = "${group}.OutbreakLibPlugin"
    apiVersion = "1.21"
    authors = listOf("OUTBREAK")
    depend = listOf()
    softDepend = listOf()
    description = rootProject.description
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP

    libraries = listOf(
        rootProject.libs.apache.commons.text.get().toString(),
        // rootProject.libs.jackson.databind.get().toString(),
        // rootProject.libs.jackson.module.kotlin.get().toString(),
        // rootProject.libs.jackson.dataformat.yaml.get().toString()
    )
}

// paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.papermc.io/repository/maven-public/") // Paper
}

dependencies {
    paperweight.paperDevBundle(rootProject.libs.versions.paper.version)
    compileOnly(rootProject.libs.apache.commons.text)
    implementation(project(":paper"))
}

kotlin {
    jvmToolchain(21)
}

tasks.shadowJar {
    // relocate("net.kyori.adventure", "${group}.net.kyori.adventure")
    // relocate("com.fasterxml.jackson", "${group}.com.fasterxml.jackson")
    // relocate("org.jetbrains.exposed", "${group}.org.jetbrains.exposed")
    // relocate("com.zaxxer", "${rootProject.group}.com.zaxxer")
    // relocate("space.outbreak.lib", "${rootProject.group}.shadedlib")
    exclude("/kotlin/")
    exclude("/kotlinx/")
    mergeServiceFiles()

    // PapiExpansion java.lang.NullPointerException: javaClass.getPackage().implementationVersion must not be null
    manifest {
        attributes("Implementation-Version" to rootProject.version)
    }

    archiveFileName.set("${rootProject.name}-${rootProject.version}.jar")
    destinationDirectory.set(file("I:\\OUTBREAK\\3.0\\test_server\\plugins\\"))
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}