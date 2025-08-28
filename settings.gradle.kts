rootProject.name = "TheBrewingProject"
pluginManagement {
    repositories {
        mavenCentral()
        maven("https://maven.neoforged.net/releases")
        gradlePluginPortal()
    }
}
include("datagenerator")
include("api")
include("core")
include("bukkit")