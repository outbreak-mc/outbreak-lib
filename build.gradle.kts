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

    version = rootProject.version

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/") // Paper
    }
}

kotlin {
    jvmToolchain(21)
}
