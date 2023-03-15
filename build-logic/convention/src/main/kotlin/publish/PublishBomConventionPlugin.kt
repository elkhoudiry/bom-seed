package publish

import getAllChildren
import getLatestPublishedVersion
import getPublishArtifactId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getByType
import tasks.BomPublishTask
import tasks.IncrementalPublishTask

class PublishBomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply(PublishConventionPlugin::class.java)
            }

            extensions.getByType<PublishingExtension>()
                .apply {
                    configurePublication(this@with)
                }

            tasks.register(
                "publishBomToGithubPackages", BomPublishTask::class.java
            ) {
                val publishTask = project.tasks.getByPath(
                    "${project.path}:publishMavenPublicationToGitHubPackagesRepository"
                ) as PublishToMavenRepository

                publication = publishTask.publication
                repository = publishTask.repository
            }
        }
    }

    private fun PublishingExtension.configurePublication(
        project: Project,
    ) {
        publications {
            getByName<MavenPublication>("maven") {
                pom {
                    properties.put("bom.version", version)
                }

                pom.withXml {
                    val dependenciesManagementNode = asNode().appendNode("dependencyManagement")
                        .appendNode("dependencies")

                    project
                        .rootProject.getAllChildren()
                        .filter {
                            it != project && it.plugins
                                .hasPlugin(PublishConventionPlugin::class.java)
                        }
                        .forEach {
                            val dependencyNode = dependenciesManagementNode.appendNode("dependency")
                            dependencyNode.appendNode("groupId", groupId)
                            dependencyNode.appendNode("artifactId", it.getPublishArtifactId())
                            dependencyNode.appendNode("version", it.getLatestPublishedVersion())
                        }
                }
            }
        }
    }
}