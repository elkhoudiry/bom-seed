import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import java.io.FileOutputStream

class DebuggableModuleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val fullModule = "$group.${project.name}"
            val generatedVersionDir = "$buildDir/generated/src/main/kotlin/${fullModule.replace(".", "/").replace("-", "/")}"

            extensions.getByType<SourceSetContainer>().apply {
                named("main") {
                    kotlin {
                        srcDir(generatedVersionDir)
                    }
                }
            }

            tasks.register("generateVersionProperties") {
                doFirst {
                    val propertiesFile = file("$generatedVersionDir/BuildConfig.kt")
                    propertiesFile.parentFile.mkdirs()
                    val out = FileOutputStream(propertiesFile)
                    out.write(
                        """
                class BuildConfig {
                    val version = "$version"
                    val module = "$fullModule"
                }
            """.trimIndent().toByteArray()
                    )
                    out.close()
                }
            }

            tasks.named("processResources") {
                dependsOn("generateVersionProperties")
            }
        }
    }

    private val org.gradle.api.tasks.SourceSet.`kotlin`: SourceDirectorySet get() =
        (this as org.gradle.api.plugins.ExtensionAware).extensions.getByName("kotlin") as SourceDirectorySet

    private fun org.gradle.api.tasks.SourceSet.`kotlin`(configure: Action<SourceDirectorySet>): Unit =
        (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("kotlin", configure)
}