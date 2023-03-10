plugins {
    id("kotlin.module")
}

dependencies {
    val bomVersion = "0.0.18"
    implementation(platform("me.elkhoudiry:bom-kt:$bomVersion"))
    implementation("me.elkhoudiry:pub-module-1")
    implementation("me.elkhoudiry:pub-module-2")
    implementation("me.elkhoudiry:pub-module-3")
}