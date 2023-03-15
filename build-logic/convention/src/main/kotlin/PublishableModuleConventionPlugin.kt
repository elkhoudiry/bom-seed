import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.getByType

class PublishableModuleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("maven-publish")
            }

            version = getVersionOrDefault(rootProject.version as String)

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
                        this.create<MavenPublication>("maven")
                            .apply {
                                groupId = rootProject.group as String
                                artifactId = project.getArtifactId()
                            }
                    }
                }

            tasks.register(
                "publishModuleToGithubPackages", IncrementalPublishToGithubRepository::class.java
            ) {
                dependsOn("${project.path}:build")
                val publishTask = project.tasks.getByPath(
                    "${project.path}:publishMavenPublicationToGitHubPackagesRepository"
                ) as PublishToMavenRepository

                publication = publishTask.publication
                repository = publishTask.repository
                publication.groupId = rootProject.group as String
            }
        }
    }

    @CacheableTask
    abstract class IncrementalPublishToGithubRepository : PublishToMavenRepository() {
        init {
            dependsOn("${project.path}:sourceCodeCheck")
            onlyIf {
                (project.extra.properties.getOrDefault("code-changed", false) as Boolean)
            }
        }

        @TaskAction
        fun perform() {
            println("[log] publishing module: ${project.getArtifactId()}")
        }
    }

    companion object {
        fun getVersionOrDefault(defaultValue: String): String {
            val version = System.getenv("PUBLISH_VERSION")

            return if (version.isNullOrBlank()) {
                defaultValue
            } else {
                version
            }
        }
    }
}