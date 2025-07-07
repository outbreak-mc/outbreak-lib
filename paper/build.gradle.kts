plugins {
    `maven-publish`
    `java-library`
}

group = "${rootProject.group}.paper"

dependencies {
    compileOnly(rootProject.libs.paper)
    compileOnlyApi(rootProject.libs.adventure.api)
    compileOnlyApi(rootProject.libs.adventure.text.minimessage)
    compileOnly(rootProject.libs.commandapi.core)
    compileOnly(rootProject.libs.commandapi.kotlin)
    implementation(project(":utils"))
}

tasks.jar {
    from(project(":utils").sourceSets.main.get().output)

    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}


publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group.toString()
            artifactId = "outbreaklib-paper"
            version = rootProject.version.toString()
            from(components["java"])
        }
    }
}