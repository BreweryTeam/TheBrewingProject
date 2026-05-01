plugins {
    `java-library`
}

group = "dev.jsinco.brewery"
version = System.getenv("VERSION")?.let {
    if (!it.matches("^v\\d+\\.\\d+\\.\\d+(-[a-z0-9.]+)?".toRegex())) {
        if (System.getenv("JITPACK")?.equals("true", true) ?: false) {
            return@let null
        }
        throw IllegalArgumentException("Invalid version name '$it', it has to follow convention v*.*.*")
    }
    it.replace("^v".toRegex(), "")
} ?: getGitHash()

fun getGitHash(): String {
    return providers.exec { commandLine("git", "rev-parse", "--short", "HEAD") }
        .standardOutput
        .asText.get().trim()
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}
