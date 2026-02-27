plugins {
    `java-library`
}

group = "dev.jsinco.brewery"
version = System.getenv("VERSION")?.let {
    if (!it.matches("^v\\d+\\.\\d+\\.\\d+(-[a-z]+)?".toRegex())) {
        throw IllegalArgumentException("Invalid version name, needs to follow convention v*.*.*")
    }
    it.replace("^v".toRegex(), "")
} ?: getGitHash()

fun getGitHash(): String {
    return providers.exec { commandLine("git", "rev-parse", "--short", "HEAD") }
        .standardOutput
        .asText.get().trim()
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}
