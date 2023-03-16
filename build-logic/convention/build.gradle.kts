plugins {
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    compileOnly(libs.android.gradlePlugin)

    testImplementation(libs.kotlin.test)
}

gradlePlugin {
    plugins {
        register("buildModule") {
            id = "build.module"
            implementationClass = "BuildConventionPlugin"
        }
        register("KotlinModule") {
            id = "kotlin.module"
            implementationClass = "KotlinModuleConventionPlugin"
        }
        register("publishModule") {
            id = "publish.module"
            implementationClass = "publish.PublishConventionPlugin"
        }
        register("publishJavaModule") {
            id = "publish.java.module"
            implementationClass = "publish.PublishJavaConventionPlugin"
        }
        register("publishBomModule") {
            id = "publish.bom.module"
            implementationClass = "publish.PublishBomConventionPlugin"
        }
        register("debuggableModule") {
            id = "debuggable.module"
            implementationClass = "DebuggableModuleConventionPlugin"
        }
    }
}
