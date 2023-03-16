package tasks

import getNewPublishVersion
import getPublishArtifactId
import org.gradle.api.GradleException
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.extra
import setLocalProperty

abstract class BomPublishTask : PublishToMavenRepository() {
    init {
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