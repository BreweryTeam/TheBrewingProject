import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    id("com.gradleup.shadow") version "8.3.5"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

dependencies {
    implementation(project(":api"))

    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("io.th0rgal:oraxen:1.183.0")

    implementation("eu.okaeri:okaeri-configs-yaml-snakeyaml:5.0.5")
    implementation("com.github.puregero:multilib:1.2.4") // Folia & ShreddedPaper support
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    processResources {
        from("src/main/resources") {
            include("plugin.yml")
            duplicatesStrategy = DuplicatesStrategy.INCLUDE

            expand (
                "version" to project.version,
            )
        }
    }

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    shadowJar {
        val pack = "dev.jsinco.brewery.bukkit.libs"

        relocate("com.github.puregero.multilib", "${pack}.multilib")
        relocate("eu.okaeri.configs", "${pack}.okaericonfigs")
        relocate("org.yaml.snakeyaml", "${pack}.snakeyaml")
        relocate("org.jetbrains.annotations", "${pack}.jetbrainsannotations")
        relocate("org.intellij.lang.annotations", "${pack}.intellijannotations")

        archiveBaseName.set(rootProject.name)
        archiveVersion.set(project.version.toString())
        archiveClassifier.set("bukkit")
    }

    jar {
        enabled = false
    }


    runServer {
        minecraftVersion("1.21.4")
    }
}
