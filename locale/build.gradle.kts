plugins {
    `maven-publish`
    `java-library`
}
group = "${rootProject.group}.locale"
version = rootProject.version

dependencies {
    compileOnly(rootProject.libs.apache.commons.text)
    compileOnly(rootProject.libs.adventure.api)
    compileOnly(rootProject.libs.adventure.text.minimessage)
    compileOnly(rootProject.libs.adventure.text.logger)
}

//publishing {
//    publications {
//        create<MavenPublication>("maven") {
//            groupId = rootProject.group.toString()
//            artifactId = "locale"
//            version = rootProject.version.toString()
//            from(components["java"])
//        }
//    }
//}
