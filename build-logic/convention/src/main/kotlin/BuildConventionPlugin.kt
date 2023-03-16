import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.provider.Provider
import java.io.File
import java.util.Properties

class BuildConventionPlugin : Plugin<Project> {
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
            localProperties.reader().close()
            properties.getProperty(key)
        }

        parent != null && recursive -> {
            parent?.getLocalProperty(key, file)
        }

        else -> null
    }
}

fun Project.getLocalPropertiesFromFile(name: String = "local"): Properties{
    val publishProperties = Properties().apply {
        val propertiesFile = File("$projectDir/$name.properties")
        if (propertiesFile.exists()){
            load(propertiesFile.reader())
            propertiesFile.reader().close()
        }
    }

    return publishProperties
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
    propertiesFile.outputStream().close()
}

fun Project.getAllChildren(): List<Project> {
    val list = arrayListOf<Project>()

    list.addAll(childProjects.map { it.value })
    list.addAll(childProjects.map { it.value.getAllChildren() }
        .flatten())

    return list
}

fun Project.getPublishGroup(): String {
    return rootProject.group as String
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

    return if (property != null && property is String) property else "0.0.0"
}

fun Project.getNewPublishVersion(): String {
    val envVersion = System.getenv("PUBLISH_REF")
    val localVersion by lazy { getLatestPublishedVersion() }

    if (!envVersion.isNullOrBlank()) {
        return getVersionFromRef(envVersion, localVersion)
    }

    return getVersionFromRef(localVersion, localVersion)
}

internal fun getVersionFromRef(
    ref: String,
    current: String
): String {
    val (major, minor) = ref.split(".")
        .map { it.toInt() }
    val (currentMajor, currentMinor, currentPatch) = current.split(".")
        .map { it.toInt() }

    if (major > currentMajor) {
        return "$major.0.0"
    }

    if (minor > currentMinor){
        return "$major.$minor.0"
    }

    return "$currentMajor.$currentMinor.${currentPatch + 1}"
}