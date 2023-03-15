package tasks

import getAllChildren
import getLatestPublishedVersion
import getNewPublishVersion
import getPublishArtifactId
import getPublishGroup
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.extra
import setLocalProperty

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
            project.rootProject.setLocalProperty(
                key = project.getPublishArtifactId(),
                value = project.getLatestPublishedVersion(),
                file = "publish.local"
            )
            updateBom()
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