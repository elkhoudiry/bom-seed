import groovy.util.Node
import groovy.util.NodeBuilder

plugins {
    kotlin("jvm") version "1.8.10"
    id("maven-publish")
}

group = "me.elkhoudiry"
version = getTagOrDefault("1.0.0-SNAPSHOT")


repositories {
    mavenCentral()
}


publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/" + System.getenv("GITHUB_REPOSITORY"))
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications {

    }
}

fun getTagOrDefault(defaultValue: String): String {
    val ref = System.getenv("GITHUB_REF")

    if (ref.isNullOrBlank()) {
        return defaultValue
    }

    if (ref.startsWith("refs/tags/")) {
        return ref.substring("refs/tags/".length)
    }

    return defaultValue
}