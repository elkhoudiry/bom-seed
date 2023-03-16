import tasks.SourceCodePublishCheckTask


plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    id("build.module") apply false
}

group = "me.elkhoudiry"
version = getLocalProperty("local.version") ?: "0.0.0"

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

tasks.register("clearPublishCache") {
    val dir = File("${projectDir.path}/.gradle")
    if (!shouldClearCache()) return@register

    dir.deleteRecursively()
}

subprojects {
    tasks.register<SourceCodePublishCheckTask>("sourceCodeCheck")
}

fun shouldClearCache(): Boolean {
    val commitMessage = System.getenv("LATEST_COMMIT_MESSAGE")
    val ref = System.getenv("GITHUB_REF")

    if (commitMessage.contains("[clear publish cache]")) {
        println("[LOG] clearing cache on commit trigger")
        return true
    }

    if (ref.startsWith("refs/tags/")) {
        println("[LOG] clearing cache on tag trigger")
        return true
    }

    return false
}
