pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    val properties = java.util.Properties()
    val user by lazy { properties.getProperty("github.user") ?: System.getenv("GITHUB_ACTOR")  }
    val token by lazy { properties.getProperty("github.token") ?: System.getenv("GITHUB_TOKEN")  }

    properties.load(File("${rootDir}/gradle.properties").reader())

    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven(url = "https://developer.huawei.com/repo/")
        google()
        mavenCentral()
        repositories {
            maven {
                url = uri("https://maven.pkg.github.com/elkhoudiry/bom-speed")
                credentials {
                    username = user
                    password = token
                }
            }
        }
    }
}

rootProject.name = "bom-elkhoudiry"
include("core:bom-kt")

include("modules:pub-module-1")
include("modules:pub-module-2")
include("modules:pub-module-3")
include("modules:consume-module")