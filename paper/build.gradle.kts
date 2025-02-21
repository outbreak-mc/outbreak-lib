plugins {
    `maven-publish`
}

dependencies {
    implementation(project(":utils"))
    // compileOnly(rootProject.libs.paper)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group.toString()
            artifactId = "outbreaklib-paper"
            version = rootProject.version.toString()
            from(components["java"])
        }
    }
}