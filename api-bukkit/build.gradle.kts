plugins {
    `tbp-module`
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(libs.paper.api)
    api(project(":api"))
}

tasks.test {
    useJUnitPlatform()
}
