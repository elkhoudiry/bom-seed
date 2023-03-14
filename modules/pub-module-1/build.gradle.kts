plugins {
    id("kotlin.module")
    id("java.publishable.module")
    id("debuggable.module")
}

dependencies {
    implementation(project(":modules:pub-module-2"))
    implementation(project(":modules:pub-module-3"))
}