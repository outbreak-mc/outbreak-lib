plugins {
    `maven-publish`
    `java-library`
}

dependencies {
    compileOnly(rootProject.libs.paper)
    compileOnly(rootProject.libs.commandapi)
    api(project(":utils"))
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