import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getByType

class JavaPublishableModuleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("publishable.module")
            }

            extensions.getByType<PublishingExtension>().apply {
                publications {
                    this.getByName<MavenPublication>("maven") {
                        from(components.getByName("java"))
                    }
                }
            }
        }
    }
}
