import dev.schlaubi.mikbot.gradle.GenerateDefaultTranslationBundleTask
import java.util.*

plugins {
    `regenbogen-ice-module`
    id("com.google.devtools.ksp") version "1.7.10-1.0.6"
    id("dev.schlaubi.mikbot.gradle-plugin") version "2.6.2"
    idea
}

group = "dev.nycode"
version = "0.6.3"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://schlaubi.jfrog.io/artifactory/mikbot/")
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))
    mikbot("dev.schlaubi", "mikbot-api", "3.14.0-SNAPSHOT")
    ksp("dev.schlaubi", "mikbot-plugin-processor", "2.3.0")
    ksp("com.kotlindiscord.kord.extensions", "annotation-processor", "1.5.5-SNAPSHOT")
    implementation(libs.marudor)
    implementation(libs.regenbogen.ice)
    implementation(libs.ktor.client.logging)
    implementation(projects.rwMutex)
    plugin("dev.schlaubi", "mikbot-health", "1.4.0")
}

mikbotPlugin {
    description.set("Plugin providing a Discord Interface to https://regenbogen-ice.de")
    pluginId.set("regenbogen-ice")
    bundle.set("regenbogen_ice")
    provider.set("Marie Ramlow")
    license.set("MIT")
}

tasks {
    val generateDefaultResourceBundle =
        task<GenerateDefaultTranslationBundleTask>("generateDefaultResourceBundle") {
            defaultLocale.set(Locale("en", "GB"))
        }
    assemblePlugin {
        dependsOn(generateDefaultResourceBundle)
    }
    assembleBot {
        bundledPlugins.set(listOf("health@1.4.0", "ktor@2.8.0"))
    }
    runBot {
        environment["DOWNLOAD_PLUGINS"] = "health,ktor"
    }
}

idea {
    module {
        // Not using += due to https://github.com/gradle/gradle/issues/8749
        sourceDirs =
            sourceDirs + file("build/generated/ksp/main/kotlin") // or tasks["kspKotlin"].destination
        testSourceDirs = testSourceDirs + file("build/generated/ksp/test/kotlin")
        generatedSourceDirs =
            generatedSourceDirs + file("build/generated/ksp/main/kotlin") + file("build/generated/ksp/test/kotlin")
    }
}

kotlin {
    sourceSets {
        all {
            languageSettings {
                enableLanguageFeature("ContextReceivers")
            }
        }
    }
}
