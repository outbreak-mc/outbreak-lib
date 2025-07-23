pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "OutbreakLib"
include("utils")
include("paper")
include("db")
include("db-shaded-utils")
include("OutbreakLibPlugin")
include("paper-shaded")
