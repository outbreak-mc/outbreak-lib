plugins {
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group.toString()
            artifactId = "outbreaklib-utils"
            version = rootProject.version.toString()
            from(components["java"])
        }
    }
}