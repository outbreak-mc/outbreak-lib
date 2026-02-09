plugins {
    `maven-publish`
    `java-library`
}

group = "${rootProject.group}.locale.db"
version = rootProject.version

dependencies {
    compileOnly(rootProject.libs.jetbrains.exposed.core)
    compileOnly(rootProject.libs.jetbrains.exposed.migration)
    compileOnly(rootProject.libs.hikaricp)
    compileOnly(rootProject.libs.adventure.api)

    implementation(project(":locale:locale"))
    implementation(project(":utils:db"))
}

// tasks.jar {
//     from(project(":locale").sourceSets.main.get().output)
//     duplicatesStrategy = DuplicatesStrategy.INCLUDE
// }

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group.toString()
            artifactId = "locale-db"
            version = rootProject.version.toString()
            from(components["java"])
        }
    }
}