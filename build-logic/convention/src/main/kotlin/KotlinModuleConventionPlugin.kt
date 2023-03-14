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

fun Project.getLocalProperty(key: String, file: String = "local.properties"): Any? {
    val properties = java.util.Properties()
    val localProperties = File("$projectDir/$file")

    return when {
        localProperties.isFile -> {
            properties.load(localProperties.reader())
            properties.getProperty(key)
        }
        parent != null -> {
            parent?.getLocalProperty(key, file)
        }
        else -> null
    }
}

fun Project.getAllChildren(): List<Project>{
    val list = arrayListOf<Project>()

    list.addAll(childProjects.map { it.value })
    list.addAll(childProjects.map { it.value.getAllChildren() }.flatten())

    return list
}

fun Project.getArtifactId(): String {
    return path.replace(":", "-").removeSuffix("-").removePrefix("-")
}