import tasks.SourceCodePublishCheckTask
import java.util.*


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

tasks.register("revertPublishToGithub") {
    val file = File("${projectDir.path}/publish.local.properties")
    val properties = Properties()

    if (!file.exists()) return@register

    properties.load(file.reader())

    for (property in properties) {
        val version = property.value
        val name = "$group/${property.key as String}"
        val command = """
            curl -L \
                -X DELETE \
                -H "Accept: application/vnd.github+json" \
                -H "Authorization: Bearer ${System.getenv("GITHUB_TOKEN")}"\
                -H "X-GitHub-Api-Version: 2022-11-28" \
                https://api.github.com/orgs/${
            System.getenv("GITHUB_REPOSITORY")
        }/packages/maven/$name/versions/${version}
        """.trimIndent()
        val builder = ProcessBuilder(command.split(' '))
        val process = builder.start()
        process.waitFor()
    }
}

subprojects {
    tasks.register<SourceCodePublishCheckTask>("sourceCodeCheck")
}
