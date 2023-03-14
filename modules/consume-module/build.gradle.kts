plugins {
    id("kotlin.module")
}

dependencies {
    val bomVersion = "0.0.0.27"
    implementation(platform("me.elkhoudiry:core-bom-kt:$bomVersion"))

    implementation("me.elkhoudiry:modules-pub-module-1") // this can be data:task-sheets
}