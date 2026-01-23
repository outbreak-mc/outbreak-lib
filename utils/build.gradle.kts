plugins {
    `maven-publish`
    `java-library`
}

dependencies {
    implementation(project(":locale:locale"))
    implementation(project(":resapi"))
    compileOnly(rootProject.libs.adventure.api)
    compileOnly(rootProject.libs.adventure.text.minimessage)
    compileOnly(rootProject.libs.adventure.text.logger)
    compileOnly(rootProject.libs.apache.commons.text)
    implementation(rootProject.libs.semver)
}
