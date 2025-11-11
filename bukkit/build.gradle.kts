import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import java.net.HttpURLConnection
import java.net.URI
import kotlin.apply
import kotlin.collections.forEach

plugins {
    `tbp-module`
    `maven-publish`

    alias(libs.plugins.shadow)
    alias(libs.plugins.run.paper)
    alias(libs.plugins.plugin.yml.bukkit)
    alias(libs.plugins.hangar.publish)
    alias(libs.plugins.modrinth.publish)
}

val supportedPaperVersions = listOf("1.21.8", "1.21.9", "1.21.10")

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
    maven("https://mvn.lumine.io/repository/maven-public/")
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
    compileOnly(libs.quickshop.hikari)
    compileOnly(libs.mythic)
    compileOnly(libs.mythic.crucible)

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
                modrinth("worldedit", "3ISh7ADm")
                modrinth("craftengine", "0.0.61")
                modrinth("vaultunlocked", "2.15.1")
                url("https://github.com/EssentialsX/Essentials/releases/download/2.21.1/EssentialsX-2.21.1.jar")
                modrinth("bolt", "1f2gAAFO")
                modrinth("quickshop-hikari", "6.2.0.10")
            }
        }
    }

    jar {
        archiveBaseName.set(rootProject.name)
        archiveClassifier.set("incomplete")
    }

    shadowJar {
        val publishing = project.gradle.startParameter.taskNames.any { it.contains("publish", true) && it.contains("maven", true) }
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

    register("postDiscordMessage") {
        val webhook = DiscordWebhook(System.getenv("DISCORD_WEBHOOK") ?: return@register)
        webhook.message = "<@&1273951212227661856>"
        webhook.embedTitle = "TheBrewingProject - v${project.version}"
        webhook.embedDescription = System.getenv("RELEASE_NOTES") + """
            
            ## The release can be found here
            **Modrinth:** https://modrinth.com/plugin/thebrewingproject/version/$version
            **Hangar:** https://hangar.papermc.io/BreweryTeam/TheBrewingProject/versions/$version
        """.trimIndent()
        webhook.send()
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
        register("brewery.command.encryption")
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
                "brewery.command.encryption" to true,
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
        "HuskClaims",
        "Bolt",
        "CraftEngine",
        "ProtocolLib",
        "PlaceholderAPI",
        "MythicLib",
        "MMOItems",
        "MythicMobs",
        "MiniPlaceholders",
        "QuickShop"
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

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN") ?: run {
        println("Could not find any Modrinth token!")
        return@modrinth
    })
    projectId.set("4zxCmDBL") // This can be the project ID or the slug. Either will work!
    versionNumber.set(project.version.toString())
    versionType.set("release") // This is the default -- can also be `beta` or `alpha`
    uploadFile.set(tasks.shadowJar)
    loaders.addAll("paper", "purpur")
    gameVersions.addAll(supportedPaperVersions)
    changelog.set(System.getenv("RELEASE_NOTES"))
}


hangarPublish {
    publications.register("plugin") {
        version = project.version as String // use project version as publication version
        id = "thebrewingproject"
        channel = "Release"
        changelog = System.getenv("RELEASE_NOTES") // optional

        // your api key.
        // defaults to the `io.papermc.hangar-publish-plugin.[publicationName].api-key` or `io.papermc.hangar-publish-plugin.default-api-key` Gradle properties
        apiKey = System.getenv("API_KEY")


        // register platforms
        platforms {
            paper {
                jar = tasks.shadowJar.flatMap { it.archiveFile }
                platformVersions = supportedPaperVersions
                dependencies {
                    url("Bolt", "https://modrinth.com/plugin/bolt") {
                        required = false
                    }
                    url("CraftEngine", "https://modrinth.com/plugin/craftengine") {
                        required = false
                    }
                    url("GriefPrevention", "https://www.spigotmc.org/resources/griefprevention.1884/") {
                        required = false
                    }
                    url("HuskClaims", "https://www.spigotmc.org/resources/huskclaims-1-17-1-21-modern-golden-shovel-land-claiming-fully-cross-server-compatible.114467/") {
                        required = false
                    }
                    url("ItemsAdder", "https://www.spigotmc.org/resources/%E2%9C%A8itemsadder%E2%AD%90emotes-mobs-items-armors-hud-gui-emojis-blocks-wings-hats-liquids.73355/") {
                        required = false
                    }
                    url("Lands", "https://www.spigotmc.org/resources/lands-%E2%AD%95-land-claim-plugin-%E2%9C%85-grief-prevention-protection-gui-management-nations-wars-1-21-support.53313/") {
                        required = false
                    }
                    url("MMOItems", "https://www.spigotmc.org/resources/mmoitems.39267/") {
                        required = false
                    }
                    url("MiniPlaceholders", "https://modrinth.com/plugin/miniplaceholders") {
                        required = false
                    }
                    url("MythicLib", "https://www.spigotmc.org/resources/mmolib-mythiclib.90306/") {
                        required = false
                    }
                    url("Nexo", "https://polymart.org/product/6901/nexo") {
                        required = false
                    }
                    url("Oraxen", "https://www.spigotmc.org/resources/%E2%98%84%EF%B8%8F-oraxen-custom-items-blocks-emotes-furniture-resourcepack-and-gui-1-18-1-21-4.72448/") {
                        required = false
                    }
                    url("PlaceholderAPI", "https://www.spigotmc.org/resources/placeholderapi.6245/") {
                        required = false
                    }
                    url("ProtocolLib", "https://www.spigotmc.org/resources/protocollib.1997/") {
                        required = false
                    }
                    url("QuickShop-Hikari", "https://modrinth.com/plugin/quickshop-hikari") {
                        required = false
                    }
                    url("Towny", "https://modrinth.com/plugin/towny") {
                        required = false
                    }
                    url("WorldGuard", "https://modrinth.com/plugin/worldguard") {
                        required = false
                    }
                    url("MythicMobs", "https://modrinth.com/plugin/mythicmobs/version/5.8.2") {
                        required = false
                    }
                }
            }
        }
    }
}

class DiscordWebhook(
    val webhookUrl: String,
    var defaultThumbnail: Boolean = true
) {

    companion object {
        private const val MAX_EMBED_DESCRIPTION_LENGTH = 4096
    }

    var message: String = "content"
    var username: String = "TheBrewingProject Updates"
    var avatarUrl: String = "https://github.com/breweryteam.png"
    var embedTitle: String = "Embed Title"
    var embedDescription: String = "Embed Description"
    var embedColor: String = "F5E083"
    var embedThumbnailUrl: String? = if (defaultThumbnail) avatarUrl else null
    var embedImageUrl: String? = null

    private fun hexStringToInt(hex: String): Int {
        val hexWithoutPrefix = hex.removePrefix("#")
        return hexWithoutPrefix.toInt(16)
    }

    private fun buildToJson(): String {
        val json = JsonObject()
        json.addProperty("username", username)
        json.addProperty("avatar_url", avatarUrl)
        json.addProperty("content", message)

        val embed = JsonObject()
        embed.addProperty("title", embedTitle)
        embed.addProperty("description", embedDescription)
        embed.addProperty("color", hexStringToInt(embedColor))

        embedThumbnailUrl?.let {
            val thumbnail = JsonObject()
            thumbnail.addProperty("url", it)
            embed.add("thumbnail", thumbnail)
        }

        embedImageUrl?.let {
            val image = JsonObject()
            image.addProperty("url", it)
            embed.add("image", image)
        }

        val embeds = JsonArray()
        createEmbeds().forEach(embeds::add)

        json.add("embeds", embeds)
        return json.toString()
    }

    private fun createEmbeds(): List<JsonObject> {
        if (embedDescription.length <= MAX_EMBED_DESCRIPTION_LENGTH) {
            return listOf(JsonObject().apply {
                addProperty("title", embedTitle)
                addProperty("description", embedDescription)
                addProperty("color", embedColor.toInt(16))
                embedThumbnailUrl?.let {
                    val thumbnail = JsonObject()
                    thumbnail.addProperty("url", it)
                    add("thumbnail", thumbnail)
                }
                embedImageUrl?.let {
                    val image = JsonObject()
                    image.addProperty("url", it)
                    add("image", image)
                }
            })
        }
        val embeds = mutableListOf<JsonObject>()
        var description = embedDescription
        while (description.isNotEmpty()) {
            val chunkLength = minOf(MAX_EMBED_DESCRIPTION_LENGTH, description.length)
            val chunk = description.substring(0, chunkLength)
            description = description.substring(chunkLength)
            embeds.add(JsonObject().apply {
                addProperty("title", embedTitle)
                addProperty("description", chunk)
                addProperty("color", embedColor.toInt(16))
                embedThumbnailUrl?.let {
                    val thumbnail = JsonObject()
                    thumbnail.addProperty("url", it)
                    add("thumbnail", thumbnail)
                }
                embedImageUrl?.let {
                    val image = JsonObject()
                    image.addProperty("url", it)
                    add("image", image)
                }
            })
        }
        return embeds
    }

    fun send() {
        val url = URI(webhookUrl).toURL()
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true
        connection.outputStream.use { outputStream ->
            outputStream.write(buildToJson().toByteArray())

            val responseCode = connection.responseCode
            println("POST Response Code :: $responseCode")
            if (responseCode == HttpURLConnection.HTTP_OK) {
                println("Message sent successfully.")
            } else {
                println("Failed to send message.")
            }
        }
    }
}