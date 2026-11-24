plugins {
    `tbp-module`
    id("java")
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")

    // For integrations
    maven("https://jitpack.io")
    maven("https://repo.oraxen.com/releases")
    maven("https://maven.devs.beer")
    maven("https://repo.nexomc.com/releases")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://repo.glaremasters.me/repository/towny/")
    maven("https://repo.minebench.de/")
    maven("https://repo.william278.net/releases")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://repo.momirealms.net/releases/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://nexus.phoenixdevt.fr/repository/maven-public/")
    maven("https://mvn.lumine.io/repository/maven-public/")
}

dependencies {
    api(project(":bukkit-api"))
    compileOnly(libs.paper.api)
    // integrations
    compileOnly(libs.bolt.bukkit)
    compileOnly(libs.bolt.common)
    compileOnly(libs.craft.engine.bukkit)
    compileOnly(libs.craft.engine.core)
    compileOnly(libs.griefprevention)
    compileOnly(libs.huskclaims.bukkit)
    compileOnly(libs.itemsadder)
    compileOnly(libs.landsapi)
    compileOnly(libs.miniplaceholders)
    compileOnly(libs.mmoitems.api)
    compileOnly(libs.mythiclib)
    compileOnly(libs.nexo)
    compileOnly(libs.oraxen)
    compileOnly(libs.placeholderapi)
    compileOnly(libs.towny)
    compileOnly(libs.worldguard.bukkit) {
        exclude("com.google.code.gson", "gson")
    }
    compileOnly(libs.quickshop.hikari)
    compileOnly(libs.mythic)
    compileOnly(libs.mythic.crucible)
    compileOnly(libs.griefdefender)
    compileOnly(libs.gsit)
    compileOnly(libs.bodyhealth)
    compileOnly(libs.jobs) {
        exclude("com.google.code.gson", "gson")
        exclude("com.gmail.nossr50.mcMMO", "mcMMO")
        exclude("com.bgsoftware", "WildStackerAPI")
    }
    compileOnly(libs.auraskills)
}

tasks.test {
    useJUnitPlatform()
}