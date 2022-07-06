plugins {
    kotlin("multiplatform") version "1.7.0"
    kotlin("plugin.serialization") version "1.7.0"
}

group = "dev.nycode"
version = "0.1.0"

repositories {
    mavenCentral()
}

kotlin {
    explicitApi()
    jvm()
    js(IR) {
        browser()
        nodejs()
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.client.kotlinx.json)
                implementation(libs.ktor.client.resources)
                implementation(libs.kotlinx.datetime)
            }
        }
        getByName("jvmMain") {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }
        getByName("jsMain") {
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }
    }
}
