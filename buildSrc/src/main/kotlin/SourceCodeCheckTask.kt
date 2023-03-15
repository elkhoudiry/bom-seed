import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.extra
import org.gradle.work.InputChanges

@CacheableTask
abstract class SourceCodeCheckTask : DefaultTask() {

    @InputFiles
    @PathSensitive(PathSensitivity.NONE)
    val inputDirs: ConfigurableFileCollection = project.files(
        project.projectDir.listFiles()
            .filter { !it.path.matches(Regex(".*build($|/.*)")) }
    )

    init {
        outputs.upToDateWhen { true }
    }

    @TaskAction
    fun execute(changes: InputChanges) {
        println("[LOG] project: ${project.name}, is incremental: ${changes.isIncremental}")

        project.extra.set("code-changed", true)
    }
}