plugins {
    `maven-publish`
    `java-library`
}

group = "${rootProject.group}.paper.shaded"

dependencies {
    compileOnly(rootProject.libs.paper)
    compileOnly(rootProject.libs.commandapi.core)
    // compileOnly(rootProject.libs.commandapi.)
    implementation(project(":locale:locale"))
    implementation(project(":resapi"))
//    implementation(project(":locale"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group.toString()
            artifactId = "utils-config"
            version = rootProject.version.toString()
            from(components["java"])
        }
    }
}