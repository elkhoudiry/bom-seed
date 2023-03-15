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

abstract class BomPublishTask : PublishToMavenRepository() {
    init {
        throw Exception("Test Exception")
        onlyIf {
            (project.rootProject.extra.properties.getOrDefault(
                SourceCodePublishCheckTask.SOURCE_CODE_CHANGED_KEY, false
            ) as Boolean)
        }
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
        }
    }

    @TaskAction
    fun perform() {
        println("[LOG] publishing bom: ${project.getPublishArtifactId()}")
    }
}