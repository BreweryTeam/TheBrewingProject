import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.bundling.Jar

plugins {
    java
    id("org.ajoberstar.grgit") version "5.3.0"
}


version = "1.3.0-" + grgit.head().abbreviatedId

allprojects {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://repo.neoforged.net/releases")
    }
}

val combinedJar by tasks.registering(Jar::class) {
    group = "build"
    description = "Combines all subprojects outputs into one jar"

    archiveBaseName.set(rootProject.name)
    archiveVersion.set(rootProject.version.toString())
    archiveClassifier.set("all")

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    dependsOn(subprojects.map { it.tasks.named("build") })

    from(subprojects.map {
        it.the<JavaPluginExtension>().sourceSets.getByName("main").output
    })
}

tasks {
    jar {
        enabled = false
    }

    build {
        dependsOn(combinedJar)
    }
}