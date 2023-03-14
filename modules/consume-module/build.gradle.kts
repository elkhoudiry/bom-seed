plugins {
    id("kotlin.module")
}

dependencies {
    val bomVersion = "0.0.0.1"
    implementation(platform("me.elkhoudiry:core-bom-kt:$bomVersion"))

    implementation("me.elkhoudiry:modules-pub-module-1") // this can be data:task-sheets
    implementation("me.elkhoudiry:modules-pub-module-2") // this can be data:warehouse
    implementation("me.elkhoudiry:modules-pub-module-3") // this can be data:seals
}