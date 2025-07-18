plugins {
    `maven-publish`
    `java-library`
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group.toString()
            artifactId = "utils"
            version = rootProject.version.toString()
            from(components["java"])
        }
    }
}

dependencies {
    compileOnlyApi(rootProject.libs.adventure.api)
    compileOnlyApi(rootProject.libs.adventure.text.minimessage)
    compileOnlyApi(rootProject.libs.adventure.text.logger)
    compileOnlyApi(rootProject.libs.apache.commons.text)
}