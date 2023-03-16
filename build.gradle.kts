import tasks.SourceCodePublishCheckTask
import java.text.SimpleDateFormat
import java.time.Clock
import java.util.*


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

    dependsOn(childTasks)
}

tasks.register("publishBomToGithub") {
    subprojects.find { it.name == "bom" }
        ?.let { bom ->
           dependsOn("${bom.path}:publishBomToGithubPackages")
        }
}

tasks.register("clearPublishCache") {
    val dir = File("${projectDir.path}/.gradle")
    if (!shouldClearCache()) return@register

    dir.deleteRecursively()
}

tasks.register("updateREADME") {
    val readmeFile = File("$projectDir/README.md")
    val readmeContent = readmeFile.readText()
    val properties = getLocalPropertiesFromFile("publish")
    val modules = properties.keys.map {
        (it as String).split(".")
            .first()
    }
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd")
    val timeFormatter = SimpleDateFormat("k:m:s.S")
    var tableContent = ""

    for (module in modules.distinct()
        .sorted()) {
        tableContent += "| $module |"

        for (releaseNumber in 1..5) {
            val release = "$module.v$releaseNumber"
            val version = properties["$release.version"] ?: "-"
            val time = (properties["$release.time"] as String?)?.toLongOrNull() ?: Clock.systemUTC()
                .millis()

            tableContent += " $version<br> ${dateFormatter.format(Date(time))}<br> " +
                    "${timeFormatter.format(Date(time))} |"
        }
        tableContent += "\n"
    }

    println("[LOG] table:\n$tableContent")

    readmeFile.outputStream()
        .write(
            readmeContent.replace(
                Regex("#### Releases(.|\n)*##### End of releases"), """
#### Releases
                
| Module | Latest | #2 | #3 | #4 | #5 |
| :----: | :----: | :----: | :----: | :----: | :----: |
$tableContent                
                
##### End of releases
            """.trimIndent()
            )
                .toByteArray()
        )


}

subprojects {
    tasks.register<SourceCodePublishCheckTask>("sourceCodeCheck")
}

fun shouldClearCache(): Boolean {
    val commitMessage = System.getenv("LATEST_COMMIT_MESSAGE") ?: ""
    val ref = System.getenv("GITHUB_REF") ?: ""

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
