plugins {
    `java-library`
}

group = "dev.jsinco.brewery"
version = project.findProperty("version")!!

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}
