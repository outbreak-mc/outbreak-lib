plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
}

tasks.wrapper {
    gradleVersion = "8.14.3"
}

group = "space.outbreak.lib.v2"
version = "2.0-SNAPSHOT"

allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "java-library")

    version = rootProject.version

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/") // Paper
    }

    dependencies {
        compileOnly(kotlin("stdlib"))
        compileOnly(kotlin("reflect"))
        compileOnly(rootProject.libs.snakeyaml)

        testImplementation(rootProject.libs.kotlin.test)
        testImplementation(rootProject.libs.snakeyaml)
        testImplementation(rootProject.libs.junit)
    }

    tasks.test {
        useJUnitPlatform()
        outputs.upToDateWhen { false }
        testLogging {
            showStandardStreams = true
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}

kotlin {
    jvmToolchain(21)
}
