import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    `tbp-module`

    id("io.github.goooler.shadow") version "8.1.7"
    id("xyz.jpenilla.run-paper") version "2.3.0"
    id("de.eldoria.plugin-yml.bukkit") version "0.7.1"
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
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:${project.findProperty("paper.version")!!}")
    compileOnly("org.projectlombok:lombok:1.18.30")
    compileOnly("org.jetbrains:annotations:24.0.0")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")

    // Integration
    compileOnly("io.th0rgal:oraxen:1.189.0")
    compileOnly("dev.lone:api-itemsadder:4.0.10")
    compileOnly("com.nexomc:nexo:1.1.0")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.13")
    compileOnly("com.github.GriefPrevention:GriefPrevention:17.0.0")
    compileOnly("com.palmergames.bukkit.towny:towny:0.100.1.9")
    compileOnly("com.github.Angeschossen:LandsAPI:7.13.1")
    compileOnly("com.acrobot.chestshop:chestshop:3.12.2")
    compileOnly("net.william278.huskclaims:huskclaims-bukkit:1.5.2")
    compileOnly("org.popcraft:bolt-bukkit:1.1.33")
    compileOnly("org.popcraft:bolt-common:1.1.33")
    compileOnly("net.momirealms:craft-engine-core:0.0.48")
    compileOnly("net.momirealms:craft-engine-bukkit:0.0.48")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("io.lumine:MythicLib-dist:1.7.1-SNAPSHOT")
    compileOnly("net.Indyuce:MMOItems-API:6.10.1-SNAPSHOT")

    implementation(project(":core"))
    api(project(":api"))
    implementation("dev.thorinwasher.schem:schem-reader:1.0.0")
    implementation("com.github.Carleslc.Simple-YAML:Simple-Yaml:1.8.4")

    testImplementation("org.junit.jupiter:junit-jupiter:5.12.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.50.0")
    testImplementation("net.kyori:adventure-nbt:4.17.0")
    testImplementation("org.xerial:sqlite-jdbc:3.47.2.0")

    annotationProcessor("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")
}

tasks {
    test {
        useJUnitPlatform()
    }


    runServer {
        minecraftVersion(project.findProperty("minecraft.version")!! as String)
        if (project.findProperty("testing.integrations")!! == "true") {
            downloadPlugins {
                modrinth("worldedit", "DlD8WKr9")
                modrinth("craftengine", "OktNyJzh")
                url("https://dev.bukkit.org/projects/chestshop/files/latest")
                url("https://dev.bukkit.org/projects/vault/files/latest")
                url("https://github.com/EssentialsX/Essentials/releases/download/2.21.0/EssentialsX-2.21.0.jar")
                url("https://api.spiget.org/v2/resources/109679/download")
                url("https://api.spiget.org/v2/resources/1997/download")
            }
        }
    }

    shadowJar {
        archiveBaseName.set(rootProject.name)
        archiveClassifier.unset()
    }
}


bukkit {
    main = "dev.jsinco.brewery.bukkit.TheBrewingProject"
    foliaSupported = false
    apiVersion = "1.21"
    authors = listOf("Jsinco", "Mitality", "Thorinwasher", "Nadwey")
    name = rootProject.name
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
            )
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("brewery") {
            children = listOf("brewery.command", "brewery.structure.create")
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
        "MMOItems"
    )
}