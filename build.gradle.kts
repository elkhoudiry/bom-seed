
plugins {
    kotlin("jvm") version "1.8.10"
}

group = "me.elkhoudiry"
version = "0.0.0.1"

tasks.register("publishToGithubPackages") {
    project.subprojects.forEach { subProject ->
        subProject.tasks.find { subTask -> subTask.name == "publishModuleToGithubPackages" }?.let {
            dependsOn(it)
        }
    }
}

tasks.withType<Test>().configureEach {
    if (!project.hasProperty("createReports")) {
        reports.html.required.set(false)
        reports.junitXml.required.set(false)
    }
}

