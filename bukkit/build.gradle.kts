import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    `tbp-module`
    `maven-publish`

    alias(libs.plugins.shadow)
    alias(libs.plugins.run.paper)
    alias(libs.plugins.plugin.yml.bukkit)
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://jitpack.io")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.oraxen.com/releases")
    maven("https://maven.devs.beer/")
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
    maven("https://storehouse.okaeri.eu/repository/maven-public/")
}

dependencies {
    implementation(project(":core"))
    api(project(":api"))

    compileOnly(libs.paper.api)

    // libraries
    compileOnly(libs.protocolLib)
    implementation(libs.schem.reader)
    implementation(libs.simple.yaml)

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
    compileOnly(libs.worldguard.bukkit)

    // other
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    // test
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)

    testImplementation(libs.adventure.nbt)
    testImplementation(libs.mockbukkit)
    testImplementation(libs.sqlite.jdbc)

    testAnnotationProcessor(libs.lombok)
}

tasks {
    test {
        useJUnitPlatform()
    }


    runServer {
        minecraftVersion(project.findProperty("minecraft.version")!! as String)
        if (project.findProperty("testing.integrations")!! == "true") {
            downloadPlugins {
                modrinth("worldedit", "DYf6XJqU")
                modrinth("craftengine", "0.0.61")
                url("https://dev.bukkit.org/projects/chestshop/files/latest")
                url("https://dev.bukkit.org/projects/vault/files/latest")
                url("https://github.com/EssentialsX/Essentials/releases/download/2.21.1/EssentialsX-2.21.1.jar")
                modrinth("bolt", "1f2gAAFO")
            }
        }
    }

    jar {
        archiveBaseName.set(rootProject.name)
        archiveClassifier.set("incomplete")
    }

    shadowJar {
        val publishing = project.gradle.startParameter.taskNames.any { it.contains("publish") }
        archiveBaseName.set(rootProject.name)
        archiveClassifier.unset()

        dependencies {
            if (!publishing) {
                exclude {
                    it.moduleGroup == "org.jetbrains.kotlin"
                            || it.moduleGroup == "org.jetbrains.kotlinx"
                            || it.moduleGroup == "org.joml"
                            || it.moduleGroup == "org.slf4j"
                }
            } else {
                include(project(":api"))
            }
        }

        exclude("org/jetbrains/annotations/**")
        exclude("org/intellij/lang/annotations/**")

        listOf(
            "com.zaxxer.hikari",
            "dev.thorinwasher.schem",
            "net.kyori.adventure.nbt",
            "net.kyori.examination",
            "org.simpleyaml",
            "org.yaml.snakeyaml",
            "eu.okaeri",
            "net.objecthunter.exp4j"
        ).forEach { relocate(it, "${project.group}.lib.$it") }
    }
}

bukkit {
    main = "dev.jsinco.brewery.bukkit.TheBrewingProject"
    foliaSupported = false
    apiVersion = "1.21"
    authors = listOf("Jsinco", "Mitality", "Thorinwasher", "Nadwey")
    name = rootProject.name
    defaultPermission = BukkitPluginDescription.Permission.Default.FALSE
    permissions {
        register("brewery.barrel.create") {
            children = listOf("brewery.barrel.access")
        }
        register("brewery.barrel.access")
        register("brewery.distillery.create") {
            children = listOf("brewery.distillery.access")
        }
        register("brewery.distillery.access")
        register("brewery.cauldron.access") {
            childrenMap = mapOf(
                "brewery.cauldron.time" to true
            )
        }
        register("brewery.cauldron.time")
        register("brewery.structure.access") {
            childrenMap = mapOf(
                "brewery.barrel.access" to true,
                "brewery.distillery.access" to true,
                "brewery.cauldron.access" to true
            )
        }
        register("brewery.structure.create") {
            default = BukkitPluginDescription.Permission.Default.TRUE
            childrenMap = mapOf(
                "brewery.structure.access" to true,
                "brewery.barrel.create" to true,
                "brewery.distillery.create" to true
            )
        }
        register("brewery.command.create")
        register("brewery.command.status")
        register("brewery.command.event")
        register("brewery.command.reload")
        register("brewery.command.info")
        register("brewery.command.seal")
        register("brewery.command.other")
        register("brewery.command.replicate")
        register("brewery.command.version")
        register("brewery.command") {
            childrenMap = mapOf(
                "brewery.command.create" to true,
                "brewery.command.status" to true,
                "brewery.command.event" to true,
                "brewery.command.reload" to true,
                "brewery.command.info" to true,
                "brewery.command.seal" to true,
                "brewery.command.other" to true,
                "brewery.command.replicate" to true,
                "brewery.command.version" to true,
            )
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("brewery.override.kick") {
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("brewery.override.effect") {}
        register("brewery.override.drunk") {}
        register("brewery.override") {
            children = listOf("brewery.override.kick", "brewery.override.effect", "brewery.override.drunk")
        }
        register("brewery") {
            children = listOf("brewery.command", "brewery.structure.create", "brewery.override")
        }
    }
    softDepend = listOf(
        "Oraxen",
        "ItemsAdder",
        "Nexo",
        "WorldGuard",
        "Lands",
        "GriefPrevention",
        "Towny",
        "ChestShop",
        "HuskClaims",
        "Bolt",
        "CraftEngine",
        "ProtocolLib",
        "PlaceholderAPI",
        "MythicLib",
        "MMOItems",
        "MiniPlaceholders"
    )
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "thebrewingproject"
            artifact(tasks["shadowJar"])
        }
    }
}