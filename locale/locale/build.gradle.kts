plugins {
    `maven-publish`
    `java-library`
}

group = "${rootProject.group}.locale.paper"
version = rootProject.version

dependencies {
    compileOnly(rootProject.libs.apache.commons.text)
    compileOnly(rootProject.libs.caffeine)
    compileOnly(rootProject.libs.paper)
    compileOnly(rootProject.libs.commandapi.core)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group.toString()
            artifactId = "locale-main"
            version = rootProject.version.toString()
            from(components["java"])
        }
    }
}