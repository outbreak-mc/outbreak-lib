plugins {
    `maven-publish`
    `java-library`
}

group = "${rootProject.group}.utils.resapi"

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group.toString()
            artifactId = "utils-resapi"
            version = rootProject.version.toString()
            from(components["java"])
        }
    }
}