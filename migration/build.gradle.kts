plugins {
    kotlin("jvm") version "2.1.0"
    id("io.github.goooler.shadow") version "8.1.7"
    id("de.eldoria.plugin-yml.bukkit") version "0.7.1"
}

group = "dev.jsinco.brewery"
version = "unspecified"

repositories {
    mavenCentral()
    maven("https://repo.jsinco.dev/releases")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

    compileOnly(project(":bukkit"))
    compileOnly(project(":api"))
    compileOnly(project(":core"))
    compileOnly("com.dre.brewery:BreweryX:3.4.10-SNAPSHOT")
}

bukkit {
    main = "dev.jsinco.brewery.migrator.TBPMigratorPlugin"
    foliaSupported = false
    apiVersion = "1.21"
    authors = listOf("Thorinwasher")
    name = "TBPMigratorPlugin"
    depend = listOf("TheBrewingProject", "BreweryX")
}

tasks.test {
    useJUnitPlatform()
}