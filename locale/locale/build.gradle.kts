plugins {
    `maven-publish`
    `java-library`
}

group = "${rootProject.group}.locale.paper"
version = rootProject.version

dependencies {
    compileOnly(rootProject.libs.apache.commons.text)
    compileOnly(rootProject.libs.paper)
    compileOnly(rootProject.libs.commandapi.core)
    implementation(project(":locale"))
}

// tasks.jar {
//     from(project(":locale").sourceSets.main.get().output)
//     duplicatesStrategy = DuplicatesStrategy.INCLUDE
// }

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group.toString()
            artifactId = "locale-paper"
            version = rootProject.version.toString()
            from(components["java"])
        }
    }
}