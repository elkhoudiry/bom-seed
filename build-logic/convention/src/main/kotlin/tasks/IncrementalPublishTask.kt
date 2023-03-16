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
                SourceCodePublishCheckTask.SOURCE_CODE_CHANGED_KEY, false
            ) as Boolean)
        }
        dependsOn("${project.path}:build")
        doLast {
            project.setLocalProperty(
                key = "version",
                value = project.getNewPublishVersion(),
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
                    key = project.getPublishArtifactId(),
                    value = project.getLatestPublishedVersion(),
                    file = "metadata"
                )
            }
        project.rootProject.extra.set(
            SourceCodePublishCheckTask.SOURCE_CODE_CHANGED_KEY,
            true
        )
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

    for (i in 1..5) {
        val release = "v$i"
        val previousRelease = "v${i - 1}"
        val version = if (i == 1) project.getLatestPublishedVersion() else
            moduleProperties["$artifactId.$previousRelease.version"] ?: "-"
        val time = if (i == 1) Clock.systemUTC()
            .millis().toString() else
            moduleProperties["$artifactId.$previousRelease.time"] ?: "-"

        root.setLocalProperty(
            key = "$artifactId.$release.time",
            value = time,
            file = fileName
        )
        root.setLocalProperty(
            key = "$artifactId.$release.version",
            value = version,
            file = fileName
        )
    }
}