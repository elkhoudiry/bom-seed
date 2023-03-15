package tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.extra
import org.gradle.work.InputChanges

abstract class SourceCodePublishCheckTask : DefaultTask() {

    @InputFiles
    @PathSensitive(PathSensitivity.NONE)
    val inputDirs: ConfigurableFileCollection = project.files(
        project.projectDir.listFiles()
            .filter {
                !it.path.matches(Regex(".*build($|/.*)")) &&
                        it.name != "publish.properties"
            }
    )

    init {
        outputs.upToDateWhen { true }
    }

    @TaskAction
    fun execute(changes: InputChanges) {
        println("[LOG] project: ${project.name}, is incremental: ${changes.isIncremental}")

        project.extra.set(SOURCE_CODE_CHANGED_KEY, true)
    }

    companion object {
        const val SOURCE_CODE_CHANGED_KEY = "code-changed"
    }
}