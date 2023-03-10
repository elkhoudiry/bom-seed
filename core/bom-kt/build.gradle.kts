import groovy.lang.Closure
import org.gradle.api.internal.artifacts.dependencies.DefaultClientModule
import org.jetbrains.kotlin.tooling.core.closure
import java.util.Properties
import java.util.regex.Pattern

plugins {
    id("kotlin.module")
    id("publishable.module")
}

publishing {
    publications {
        create<MavenPublication>(project.name) {this
            pom {
                val versionPropertiesFile = File("$projectDir/src/main/versions/_versions.properties")
                val versionProperties = Properties()
                versionProperties.load(versionPropertiesFile.reader())

                versionProperties.forEach {
                    properties.put("${it.key}", it.value as String)
                }

                distributionManagement {
                    for (propertyFile in File("$projectDir/src/main/versions").listFiles()) {
                        if (propertyFile.name == "_versions.properties") {
                            continue
                        }

                        val groupIdFromFile = propertyFile.name.replace(".properties", "")

                        val versions = Properties()
                        versions.load(propertyFile.reader())

                        versions.forEach {
                            val lastVersion = if (it.value is String && (it.value as String).matches(Regex("/\\d.*/"))) it.value else "\${$it.value}"

                        }
                    }
                }
            }
        }
    }
}