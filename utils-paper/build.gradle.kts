plugins {
    `maven-publish`
    `java-library`
}

group = "${rootProject.group}.paper.shaded"

dependencies {
    compileOnly(rootProject.libs.paper)
    compileOnly(rootProject.libs.commandapi.core)
    // compileOnly(rootProject.libs.commandapi.)
    compileOnly(project(":locale"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group.toString()
            artifactId = "utils-paper"
            version = rootProject.version.toString()
            from(components["java"])
        }
    }
}