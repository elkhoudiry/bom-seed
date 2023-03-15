plugins {
    id("kotlin.module")
    id("publishable.module")
}

publishing {
    publications {
        getByName<MavenPublication>("maven") {
            pom {
                properties.put(
                    "bom.version",
                    PublishableModuleConventionPlugin.getVersionOrDefault(rootProject.version as String)
                )
            }

            pom.withXml {
                val dependenciesManagementNode = asNode().appendNode("dependencyManagement").appendNode("dependencies")

                rootProject.getAllChildren().filter { it != project }.forEach {
                    if (it.plugins.hasPlugin("publishable.module")){
                        val dependencyNode = dependenciesManagementNode.appendNode("dependency")
                        dependencyNode.appendNode("groupId", groupId)
                        dependencyNode.appendNode("artifactId", it.getArtifactId())
                        dependencyNode.appendNode("version", "\${bom.version}")
                    }
                }
            }
        }
    }
}