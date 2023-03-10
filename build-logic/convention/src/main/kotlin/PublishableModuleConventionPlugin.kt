import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import java.io.File

class PublishableModuleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("maven-publish")
            }

            version = getTagOrDefault(rootProject.version as String)

            extensions.getByType<PublishingExtension>().apply {
                repositories {
                    maven {
                        val repository =
                            getLocalProperty("github.repository") as String? ?: System.getenv("GITHUB_REPOSITORY")
                        val user = getLocalProperty("github.user") as String? ?: System.getenv("GITHUB_ACTOR")
                        val token = getLocalProperty("github.token") as String? ?: System.getenv("GITHUB_TOKEN")

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
                        from(components.getByName("java"))
                    }
                }
            }

            tasks.register(
                "publishModuleToGithubPackages", IncrementalPublishToGithubRepository::class.java
            ) {
                inputDir = file("src/main")
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

    open class IncrementalPublishToGithubRepository : PublishToMavenRepository() {
        @InputDirectory
        lateinit var inputDir: File

        @OutputDirectory
        val generatedFileDir = project.file("${project.buildDir}/libs")

        @TaskAction
        fun perform(inputs: IncrementalTaskInputs) {
            println("publishing project: ${project.name}")
        }
    }

    companion object {
        fun getTagOrDefault(defaultValue: String): String {
            val ref = System.getenv("GITHUB_REF")

            if (ref.isNullOrBlank()) {
                return defaultValue
            }

            if (ref.startsWith("refs/tags/")) {
                return ref.substring("refs/tags/".length)
            }

            return defaultValue
        }
    }
}