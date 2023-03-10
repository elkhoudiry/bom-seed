plugins {
    id("kotlin.module")
}

dependencies {
    implementation(project(":modules:pub-module-1"))
    implementation(project(":modules:pub-module-2"))
    implementation(project(":modules:pub-module-3"))
}