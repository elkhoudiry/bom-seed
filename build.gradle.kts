import tasks.SourceCodePublishCheckTask

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    id("build.module") apply false
}

group = "me.elkhoudiry"
version = getLocalProperty("local.version") ?: "0.0.1"

tasks.withType<Test>()
    .configureEach {
        if (!project.hasProperty("createReports")) {
            reports.html.required.set(false)
            reports.junitXml.required.set(false)
        }
    }


tasks.register("publishToGithub") {
    val childTasks by lazy {
        project.subprojects.filter { subProject ->
            subProject.name != "bom" && subProject.plugins.hasPlugin("publish.module")
        }
            .map { "${it.path}:publishToGithubPackages" }
    }

    subprojects.find { it.name == "bom" }
        ?.let { bom ->
            bom.tasks["publishBomToGithubPackages"].dependsOn(childTasks)
            dependsOn(bom.tasks["publishBomToGithubPackages"])
        }
}

subprojects {
    tasks.register<SourceCodePublishCheckTask>("sourceCodeCheck")
}
