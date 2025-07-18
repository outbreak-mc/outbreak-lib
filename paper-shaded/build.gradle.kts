plugins {
    `maven-publish`
    `java-library`
}

group = "${rootProject.group}.paper.shaded"

dependencies {
    compileOnly(rootProject.libs.paper)
    // compileOnly(project(":utils"))
}

tasks.jar {
    from(project(":utils").sourceSets.main.get().output)

    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}


publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group.toString()
            artifactId = "paper-shaded"
            version = rootProject.version.toString()
            from(components["java"])
        }
    }
}