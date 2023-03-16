package tasks

import getAllChildren
import getLatestPublishedVersion
import getLocalPropertiesFromFile
import getNewPublishVersion
import getPublishArtifactId
import org.gradle.api.Project
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.extra
import setLocalProperty
import java.time.Clock

abstract class IncrementalPublishTask : PublishToMavenRepository() {
    init {
        dependsOn("${project.path}:sourceCodeCheck")
        onlyIf {
            (project.extra.properties.getOrDefault(
                "code-changed", false
            ) as Boolean)
        }
        dependsOn("${project.path}:build")
        doLast {
            project.setLocalProperty(
                values = mapOf("version" to project.getNewPublishVersion()),
                file = "publish"
            )
            updateBom()
            project.updatePublishProperties()
        }
    }

    @TaskAction
    fun perform() {
        println("[LOG] publishing module: ${project.getPublishArtifactId()}")
    }

    private fun updateBom() {
        project.rootProject.getAllChildren()
            .find { it.name == "bom" }
            ?.let {
                it.setLocalProperty(
                    values = mapOf(
                        project.getPublishArtifactId() to project
                            .getLatestPublishedVersion()
                    ),
                    file = "metadata"
                )
            }
        project.rootProject.extra.set("code-changed", true)
    }
}

internal fun Project.updatePublishProperties() {
    val fileName = "publish"
    val root = project.rootProject
    val artifactId = project.getPublishArtifactId()

    val properties = project.rootProject.getLocalPropertiesFromFile(fileName)
    val moduleProperties = properties.filter {
        (it.key as String).startsWith(project.getPublishArtifactId())
    }
    val values = mutableMapOf<String, String>()
    for (i in 1..5) {
        val release = "v$i"
        val previousRelease = "v${i - 1}"
        val version = if (i == 1) project.getLatestPublishedVersion() else
            moduleProperties["$artifactId.$previousRelease.version"] ?: "-"
        val time = if (i == 1) Clock.systemUTC()
            .millis()
            .toString() else
            moduleProperties["$artifactId.$previousRelease.time"] ?: "-"

        values["$artifactId.$release.time"] = time.toString()
        values["$artifactId.$release.version"] = version.toString()
    }

    root.setLocalProperty(values = values, file = fileName)
}