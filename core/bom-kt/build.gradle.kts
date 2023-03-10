import java.util.Properties

plugins {
    id("kotlin.module")
    id("publishable.module")
}

publishing {
    publications {
        getByName<MavenPublication>("maven") {
            pom {
                val versionPropertiesFile = File("$projectDir/src/main/versions/_versions.properties")
                val versionProperties = Properties()
                versionProperties.load(versionPropertiesFile.reader())

                versionProperties.forEach {
                    properties.put("${it.key}", it.value as String)
                }
            }

            pom.withXml {
                val dependenciesManagementNode = asNode().appendNode("dependencyManagement").appendNode("dependencies")

                for (propertyFile in File("$projectDir/src/main/versions").listFiles()!!) {
                    if (propertyFile.name == "_versions.properties") {
                        continue
                    }

                    val groupIdFromFile = propertyFile.name.replace(".properties", "")

                    val versions = Properties()
                    versions.load(propertyFile.reader())

                    versions.forEach {
                        val lastVersion =
                            if (it.value is String && (it.value as String).matches(Regex("/\\d.*/"))) it.value else "\${$it.value}"
                        val dependencyNode = dependenciesManagementNode.appendNode("dependency")
                        dependencyNode.appendNode("groupId", groupIdFromFile)
                        dependencyNode.appendNode("artifactId", it.key as String)
                        dependencyNode.appendNode("version", lastVersion as String)
                    }
                }
            }
        }
    }
}