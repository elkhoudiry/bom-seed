import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class KotlinModuleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val libs =
                extensions.getByType<VersionCatalogsExtension>().named("libs")

            pluginManager.apply {
                apply("kotlin")
                apply(libs.getPlugin("kotlin-serialization"))
            }

            dependencies {
                add(
                    "implementation",
                    libs.getLibrary("kotlinx.serialization.json")
                )
                add("implementation", libs.getLibrary("kotlinx.datetime"))
                add("testImplementation", libs.getLibrary("kotlin.test"))
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