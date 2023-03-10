
plugins {
    kotlin("jvm") version "1.8.10"
}

group = "me.elkhoudiry"
version = "0.1.0"

tasks.register("publishToGithubPackages") {
    project.subprojects.forEach { subProject ->
        subProject.tasks.find { subTask -> subTask.name == "publishModuleToGithubPackages" }?.let {
            dependsOn(it)
        }
    }
}