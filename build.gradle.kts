plugins {
    id("org.ajoberstar.grgit") version "5.3.0"
}

version = "1.3.0-" + grgit.head().abbreviatedId