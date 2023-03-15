plugins {
    id("kotlin.module")
    id("publish.java.module")
    id("debuggable.module")
}

dependencies {
    implementation(project(":modules:pub-module-2"))
    implementation(project(":modules:pub-module-3"))
}