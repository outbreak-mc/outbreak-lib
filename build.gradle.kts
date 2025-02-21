plugins {
    alias(libs.plugins.kotlin.jvm)
    // alias(libs.plugins.shadow)
    // alias(libs.plugins.buildconfig)
}

tasks.wrapper {
    gradleVersion = "8.13"
}

group = "space.outbreak.lib"
version = "1.0-SNAPSHOT"

allprojects {
    apply(plugin = "kotlin")

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation(rootProject.libs.adventure.api)
        implementation(rootProject.libs.adventure.text.minimessage)
        implementation(rootProject.libs.adventure.text.logger)
        implementation(rootProject.libs.apache.commons.text)

        implementation(rootProject.libs.jackson.databind)
        implementation(rootProject.libs.jackson.module.kotlin)
        implementation(rootProject.libs.jackson.dataformat.yaml)
    }
}

dependencies {
    implementation(project(":utils"))
    implementation(project(":paper"))
}

tasks.jar {
    from(project(":utils").sourceSets.main.get().output)
    from(project(":paper").sourceSets.main.get().output)

    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

kotlin {
    jvmToolchain(21)
}

// tasks.shadowJar {
//     // relocate("net.kyori.adventure", "${rootProject.group}.net.kyori.adventure")
//     // relocate("com.fasterxml.jackson", "${rootProject.group}.com.fasterxml.jackson")
//     // relocate("org.apache.commons.text", "${rootProject.group}.org.apache.commons.text")
//     mergeServiceFiles()
//
//     manifest {
//         attributes("Implementation-Version" to rootProject.version)
//     }
//
//     archiveFileName.set("${rootProject.name}-${rootProject.version}.jar")
//     // destinationDirectory.set(file("I:\\OUTBREAK\\3.0\\test_server\\plugins\\"))
// }
//
// tasks.assemble {
//     dependsOn(tasks.shadowJar)
// }
