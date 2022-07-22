plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin("gradle-plugin", version = "1.7.10"))
    implementation(kotlin("serialization", version = "1.7.10"))
    implementation("org.jetbrains.kotlinx:atomicfu-gradle-plugin:0.18.3")
}
