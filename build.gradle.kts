plugins {
    id("java")
    id("io.github.goooler.shadow") version "8.1.7"
}

group = "dev.jsinco.brewery"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://jitpack.io")
    maven("https://repo.oraxen.com/releases")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.2-R0.1-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.30")

    //
    compileOnly("io.th0rgal:oraxen:1.163.0")

    implementation("com.github.Carleslc.Simple-YAML:Simple-Yaml:1.8.4")
    implementation("dev.thorinwasher.schem:schem-reader:dev")
    compileOnly("org.jetbrains:annotations:24.0.0")

    annotationProcessor("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.8.0")
    testImplementation("net.kyori:adventure-nbt:4.17.0")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks.test {
    useJUnitPlatform()
}