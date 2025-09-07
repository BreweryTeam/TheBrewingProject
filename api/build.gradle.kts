plugins {
    `tbp-module`
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.gson)
    compileOnly(libs.guava)
    compileOnly(libs.adventure.api)
    compileOnly(libs.adventure.text.minimessage)
    compileOnly(libs.exp4j)

    // test
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}
