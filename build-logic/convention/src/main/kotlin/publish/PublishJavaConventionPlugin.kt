package publish

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getByType
import tasks.IncrementalPublishTask

class PublishJavaConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply(PublishConventionPlugin::class.java)
            }

            extensions.getByType<PublishingExtension>().apply {
                publications {
                    this.getByName<MavenPublication>("maven") {
                        from(components.getByName("java"))
                    }
                }
            }

            tasks.register(
                "publishToGithubPackages", IncrementalPublishTask::class.java
            ) {
                val publishTask = project.tasks.getByPath(
                    "${project.path}:publishMavenPublicationToGitHubPackagesRepository"
                ) as PublishToMavenRepository

                publication = publishTask.publication
                repository = publishTask.repository
            }
        }
    }
}
