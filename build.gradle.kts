plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
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
    apply(plugin = "java-library")

    version = rootProject.version

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/") // Paper
    }

    dependencies {
        compileOnlyApi(rootProject.libs.jackson.databind)
        compileOnlyApi(rootProject.libs.jackson.module.kotlin)
        compileOnlyApi(rootProject.libs.jackson.dataformat.yaml)
    }
}

kotlin {
    jvmToolchain(21)
}
