pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven(url = "https://developer.huawei.com/repo/")
        google()
        mavenCentral()
    }
}

rootProject.name = "bom-elkhoudiry"
include("core:bom-kt")

include("modules:pub-module-1")
include("modules:pub-module-2")
include("modules:pub-module-3")
include("modules:consume-module")