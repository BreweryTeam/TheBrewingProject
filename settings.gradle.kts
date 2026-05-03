rootProject.name = "TheBrewingProject"

pluginManagement {
    repositories {
        mavenCentral()
        maven("https://maven.neoforged.net/releases")
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include("datagenerator")
include("api")
include("core")
include("bukkit-api")
include("bukkit-impl")
include("migration")
