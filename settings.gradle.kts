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
include("utils:db")
include("utils:paper")
include("utils:config")

include("locale:locale")
include("locale:db")
include("locale:paper")

include("OutbreakLibPlugin")
include("resapi")
