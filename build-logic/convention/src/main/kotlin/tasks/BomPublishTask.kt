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
        println("[LOG] init BOM task")

        onlyIf {
            val check = project.rootProject.extra.properties.getOrDefault(
                SourceCodePublishCheckTask.SOURCE_CODE_CHANGED_KEY, false
            ) as Boolean
            println("[LOG] BOM Check: $check")
            check
        }
        doLast {
            project.setLocalProperty(
                values = mapOf("version" to project.getNewPublishVersion()),
                file = "publish"
            )
            project.updatePublishProperties()
        }
    }

    @TaskAction
    fun perform() {
        println("[LOG] publishing bom: ${project.getPublishArtifactId()}")
    }
}