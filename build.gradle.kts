import java.util.Properties

plugins {
    kotlin("jvm") version "1.8.10"
}

group = "me.elkhoudiry"
version = getLocalProperty("local.version")!!

tasks.register("publishToGithubPackages") {
    project.subprojects.forEach { subProject ->
        subProject.tasks.find { subTask -> subTask.name == "publishModuleToGithubPackages" }?.let {
            dependsOn(it)
        }
    }
}

tasks.withType<Test>().configureEach {
    if (!project.hasProperty("createReports")) {
        reports.html.required.set(false)
        reports.junitXml.required.set(false)
    }
}

subprojects {
    tasks.register<SourceCodeCheckTask>("sourceCodeCheck")
}

fun Project.getLocalProperty(key: String, file: String = "local.properties"): Any? {
    val properties = Properties()
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