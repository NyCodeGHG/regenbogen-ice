plugins {
    `regenbogen-ice-module`
    id("kotlinx-atomicfu")
}

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.assertk)
    implementation("org.jetbrains.kotlinx", "atomicfu-jvm", "0.18.3")
}
