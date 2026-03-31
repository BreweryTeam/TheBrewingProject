
plugins {
    `tbp-module`
    id("java-test-fixtures")
}

repositories {
    mavenCentral()
    maven("https://repo.faststats.dev/releases")
}

dependencies {
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.gson)
    compileOnly(libs.guava)
    compileOnly(libs.adventure.api)
    compileOnly(libs.adventure.text.minimessage)
    compileOnly(libs.exp4j)
    compileOnly(libs.faststats.core)
    // test
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.gson)
    testImplementation(libs.guava)
    testImplementation(libs.adventure.api)

    testFixturesImplementation(libs.junit.jupiter)
    testFixturesImplementation(libs.adventure.api)
}

tasks.test {
    useJUnitPlatform()
}
