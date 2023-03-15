package publish

import getPublishArtifactId
import getLocalProperty
import getNewPublishVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import tasks.IncrementalPublishTask

class PublishConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("maven-publish")
            }

            extensions.getByType<PublishingExtension>()
                .apply {
                    repositories {
                        maven {
                            val repository =
                                getLocalProperty("github.repository") as String?
                                    ?: System.getenv("GITHUB_REPOSITORY")
                            val user = getLocalProperty("github.user") as String?
                                ?: System.getenv("GITHUB_ACTOR")
                            val token = getLocalProperty("github.token") as String?
                                ?: System.getenv("GITHUB_TOKEN")

                            name = "GitHubPackages"
                            url = uri("https://maven.pkg.github.com/$repository")
                            credentials {
                                username = user
                                password = token
                            }
                        }
                    }

                    publications {
                        this.create<MavenPublication>("maven") {
                            groupId = rootProject.group as String
                            artifactId = project.getPublishArtifactId()
                            version = project.getNewPublishVersion()
                        }
                    }
                }
        }
    }
}