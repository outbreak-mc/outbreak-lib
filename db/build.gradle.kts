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
}

tasks.jar {
    from(project(":db").sourceSets.main.get().output)

    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}


publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group.toString()
            artifactId = "outbreaklib-db"
            version = rootProject.version.toString()
            from(components["java"])
        }
    }
}