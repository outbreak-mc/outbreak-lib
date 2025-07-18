plugins {
    `maven-publish`
    `java-library`
}

group = "${rootProject.group}.db"

dependencies {
    compileOnly(rootProject.libs.jetbrains.exposed.core)
    compileOnly(rootProject.libs.jetbrains.exposed.migration)
    compileOnly(rootProject.libs.hikaricp)
    compileOnly(project(":utils"))

    testImplementation(project(":utils"))
    testImplementation(rootProject.libs.h2)
    testImplementation(rootProject.libs.jetbrains.exposed.core)
    testImplementation(rootProject.libs.jetbrains.exposed.jdbc)
    testImplementation(rootProject.libs.jetbrains.exposed.dao)
    testImplementation(rootProject.libs.jetbrains.exposed.migration)
    testImplementation(rootProject.libs.hikaricp)
    testImplementation(rootProject.libs.adventure.api)
    testImplementation(rootProject.libs.adventure.text.logger)
    testImplementation(rootProject.libs.adventure.text.minimessage)
    testImplementation(rootProject.libs.apache.commons.text)
    testImplementation(rootProject.libs.adventure.text.serializer.ansi)
}

tasks.jar {
    from(project(":db").sourceSets.main.get().output)

    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}


publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group.toString()
            artifactId = "db"
            version = rootProject.version.toString()
            from(components["java"])
        }
    }
}