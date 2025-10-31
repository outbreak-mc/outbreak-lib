plugins {
    `maven-publish`
    `java-library`
}

group = "${rootProject.group}.api"
version = rootProject.version

dependencies {
    implementation(project(":utils"))
    implementation(project(":locale"))
    implementation(project(":locale-db"))

    compileOnly(rootProject.libs.adventure.api)
    compileOnly(rootProject.libs.adventure.text.minimessage)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group.toString()
            artifactId = "api"
            version = rootProject.version.toString()
            from(components["java"])
        }
    }
}
