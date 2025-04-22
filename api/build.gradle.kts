plugins {
    id("java")
}

group = "dev.jsinco.brewery"
version = "1.2.0-38974eb"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    compileOnly("org.jetbrains:annotations:24.0.0")
    compileOnly("com.google.code.gson:gson:2.12.1")
    compileOnly("com.google.guava:guava:33.4.0-jre")
}

tasks.test {
    useJUnitPlatform()
}