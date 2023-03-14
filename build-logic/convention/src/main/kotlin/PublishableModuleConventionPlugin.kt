import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.work.Incremental
import org.gradle.work.InputChanges

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
                    this.create<MavenPublication>("maven").apply {
                        groupId = rootProject.group as String
                        artifactId = project.getArtifactId()
                    }
                }
            }

            tasks.register(
                "publishModuleToGithubPackages", IncrementalPublishToGithubRepository::class.java
            ) {
                inputDir.setFrom(
                    projectDir.listFiles().filter { !it.path.matches(Regex(".*build($|/.*)")) }
                )
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

    abstract class IncrementalPublishToGithubRepository : PublishToMavenRepository() {
        @get:Incremental
        @get:PathSensitive(PathSensitivity.NAME_ONLY)
        @get:InputFiles
        abstract val inputDir: ConfigurableFileCollection

        init {
            outputs.upToDateWhen {
                true
            }
        }

        @TaskAction
        fun perform(inputs: InputChanges) {
            println("[log] publishing module: ${project.getArtifactId()}")
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