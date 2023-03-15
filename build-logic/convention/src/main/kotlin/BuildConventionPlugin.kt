import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.provider.Provider
import java.io.File

class BuildConventionPlugin: Plugin<Project> {
    override fun apply(target: Project) = Unit
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
    return library.get()
        .get().pluginId
}

fun Project.getLocalProperty(
    key: String,
    file: String = "local.properties",
    recursive: Boolean = true
): Any? {
    val properties = java.util.Properties()
    val localProperties = File("$projectDir/$file")

    return when {
        localProperties.isFile -> {
            properties.load(localProperties.reader())
            properties.getProperty(key)
        }

        parent != null && recursive -> {
            parent?.getLocalProperty(key, file)
        }

        else -> null
    }
}

fun Project.setLocalProperty(
    key: String,
    value: Any,
    file: String = ""
) {
    val properties = java.util.Properties()
    val fileName = if (file.isBlank()) "local.properties" else "$file.properties"
    val propertiesFile = File("$projectDir/$fileName")

    if (!propertiesFile.exists()) {
        propertiesFile.createNewFile()
    }

    properties.load(propertiesFile.reader())
    properties[key] = value

    properties.store(propertiesFile.outputStream(), null)
}

fun Project.getAllChildren(): List<Project> {
    val list = arrayListOf<Project>()

    list.addAll(childProjects.map { it.value })
    list.addAll(childProjects.map { it.value.getAllChildren() }
        .flatten())

    return list
}

fun Project.getPublishArtifactId(): String {
    return path.replace(":", "-")
        .removeSuffix("-")
        .removePrefix("-")
}

fun Project.getLatestPublishedVersion(): String {
    val property = getLocalProperty(
        key = "version",
        file = "publish.properties",
        recursive = false
    )

    return if (property is String) property else rootProject.version.toString()
}

fun Project.getNewPublishVersion(): String {
    val envVersion = System.getenv("PUBLISH_VERSION")
    val localVersion by lazy { getLatestPublishedVersion() }

    if (!envVersion.isNullOrBlank()) {
        return envVersion
    }

    return localVersion.replaceAfterLast(
        delimiter = ".",
        replacement = "${
            localVersion.split(".")
                .last()
                .toInt() + 1
        }"
    )
}