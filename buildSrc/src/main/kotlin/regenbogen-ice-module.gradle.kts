import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("test-junit5"))
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "18"
    }
    withType<Test> {
        useJUnitPlatform()
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(18))
    }
}
