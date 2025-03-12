plugins {
    `maven-publish`
    `java-library`
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

dependencies {
    compileOnlyApi(rootProject.libs.adventure.api)
    compileOnlyApi(rootProject.libs.adventure.text.minimessage)
    compileOnlyApi(rootProject.libs.adventure.text.logger)

    api(rootProject.libs.apache.commons.text)

    api(rootProject.libs.jackson.databind)
    api(rootProject.libs.jackson.module.kotlin)
    api(rootProject.libs.jackson.dataformat.yaml)
}