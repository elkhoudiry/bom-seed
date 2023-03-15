package tasks

import getAllChildren
import getNewPublishVersion
import getPublishArtifactId
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.extra
import setLocalProperty

abstract class BomPublishTask : PublishToMavenRepository() {
    init {
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
        }
    }

    @TaskAction
    fun perform() {
        println("[LOG] publishing bom: ${project.getPublishArtifactId()}")
    }
}