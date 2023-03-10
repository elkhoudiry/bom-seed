import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import java.io.File

class KotlinModuleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val libs =
                extensions.getByType<VersionCatalogsExtension>().named("libs")

            pluginManager.apply {
                apply("kotlin")
            }

            dependencies {

            }
        }
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun VersionCatalog.getLibrary(name: String): Provider<MinimalExternalModuleDependency> {
    val library = findLibrary(name)

    if (!library.isPresent) {
        throw IllegalAccessException(
            "Couldn't find library: $name, at VersionCatalog file"
        )
    }
    return library.get()
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun VersionCatalog.getPlugin(name: String): String {
    val library = findPlugin(name)

    if (!library.isPresent) {
        throw IllegalAccessException(
            "Couldn't find plugin: $name, at VersionCatalog file"
        )
    }
    return library.get().get().pluginId
}

fun Project.getLocalProperty(key: String, file: String = "local.properties"): Any {
    val properties = java.util.Properties()
    val localProperties = File("$projectDir/$file")
    if (localProperties.isFile) {
        properties.load(localProperties.reader())
    } else if (parent != null) {
        return parent!!.getLocalProperty(key, file)
    } else error("Local property not found")

    return properties.getProperty(key)
}