import dev.schlaubi.mikbot.gradle.GenerateDefaultTranslationBundleTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Locale

plugins {
    kotlin("jvm") version "1.7.0"
    kotlin("plugin.serialization") version "1.7.0"
    id("com.google.devtools.ksp") version "1.7.10-1.0.6"
    id("dev.schlaubi.mikbot.gradle-plugin") version "2.4.1"
}

group = "dev.nycode"
version = "0.1.0"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))
    mikbot("dev.schlaubi", "mikbot-api", "3.3.0-SNAPSHOT")
    ksp("dev.schlaubi", "mikbot-plugin-processor", "2.2.0")
    ksp("com.kotlindiscord.kord.extensions", "annotation-processor", "1.5.5-MIKBOT-SNAPSHOT")
    implementation(projects.client)
    implementation(libs.marudor)
}

mikbotPlugin {
    description.set("Plugin providing a Discord Interface to https://regenbogen-ice.de")
    pluginId.set("regenbogen-ice")
    bundle.set("regenbogen_ice")
    provider.set("Marie Ramlow")
    license.set("MIT")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "18"
    }
    val generateDefaultResourceBundle = task<GenerateDefaultTranslationBundleTask>("generateDefaultResourceBundle") {
        defaultLocale.set(Locale("en", "GB"))
    }

    assemblePlugin {
        dependsOn(generateDefaultResourceBundle)
    }
}
