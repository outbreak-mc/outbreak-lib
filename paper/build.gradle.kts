plugins {
    `maven-publish`
}

dependencies {
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