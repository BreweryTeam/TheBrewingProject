plugins {
    java
}

group = "dev.jsinco.brewery"
version = "1.0-SNAPSHOT"

tasks {
    jar {
        enabled = false
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "idea")

    group = rootProject.group
    version = rootProject.version

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    repositories {
        mavenCentral()

        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://jitpack.io")
        maven("https://repo.oraxen.com/releases")
        maven("https://storehouse.okaeri.eu/repository/maven-public/")
        maven("https://repo.oraxen.com/releases")
        maven("https://repo.clojars.org/")
    }

    dependencies {
        implementation("org.jetbrains:annotations:26.0.1")

        compileOnly("org.projectlombok:lombok:1.18.36")
        annotationProcessor("org.projectlombok:lombok:1.18.36")
    }
}

