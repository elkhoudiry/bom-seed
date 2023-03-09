import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.gradle.kotlin.dsl.getByType
import java.io.File

class PublishableModuleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            pluginManager.apply {
                apply("maven-publish")
            }

            extensions.getByType<PublishingExtension>().apply {
                repositories {
                    maven {
                        name = "GitHub"
                        url = uri("https://maven.pkg.github.com/" + System.getenv("GITHUB_REPOSITORY"))
                        credentials {
                            username = ""
                            password = System.getenv("GITHUB_TOKEN")
                        }
                    }
                }

                publications {

                }
            }

            tasks.register(
                "incrementalPublishToMavenRepository",
                IncrementalPublishToMavenRepository::class.java
            ) {
                inputDir = file("src")
                val publishTask = project.tasks.getByPath(
                    ":publishMavenPublicationToGitHubPackagesRepository"
                ) as PublishToMavenRepository

                publication = publishTask.publication
                repository = publishTask.repository
            }
        }
    }
}

class IncrementalPublishToMavenRepository : PublishToMavenRepository() {
    @InputDirectory
    lateinit var inputDir: File

    @OutputDirectory
    val generatedFileDir = project.file("${project.buildDir}/libs")

    @TaskAction
    fun perform(inputs: IncrementalTaskInputs) {
        println("publishing project: ${project.name}")
    }
}