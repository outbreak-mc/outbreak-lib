plugins {
    `maven-publish`
    `java-library`
}

group = "${rootProject.group}.paper.shaded"

dependencies {
    compileOnly(rootProject.libs.paper)
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