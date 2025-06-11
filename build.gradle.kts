plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
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
        compileOnly(kotlin("stdlib"))
        compileOnly(rootProject.libs.snakeyaml)
    }
}

kotlin {
    jvmToolchain(21)
}
