import dev.schlaubi.mikbot.gradle.GenerateDefaultTranslationBundleTask
import java.util.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.8.0"
  kotlin("plugin.serialization") version "1.8.0"
  id("com.google.devtools.ksp") version "1.8.0-1.0.9"
  id("dev.schlaubi.mikbot.gradle-plugin") version "2.6.4"
  idea
}

group = "dev.nycode"

version = "0.6.6"

repositories {
  mavenCentral()
  maven("https://oss.sonatype.org/content/repositories/snapshots")
  maven("https://schlaubi.jfrog.io/artifactory/mikbot/")
}

dependencies {
  compileOnly(kotlin("stdlib-jdk8"))
  mikbot("dev.schlaubi", "mikbot-api", "3.16.0-SNAPSHOT")
  ksp("dev.schlaubi", "mikbot-plugin-processor", "2.3.0")
  ksp("com.kotlindiscord.kord.extensions", "annotation-processor", "1.5.5-SNAPSHOT")
  implementation(libs.marudor)
  implementation(libs.regenbogen.ice)
  implementation(libs.ktor.client.logging)
  plugin("dev.schlaubi", "mikbot-health", "1.6.0")
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
  assemblePlugin { dependsOn(generateDefaultResourceBundle) }
  assembleBot { bundledPlugins.set(listOf("health@1.5.0", "ktor@2.8.0")) }
  runBot { environment["DOWNLOAD_PLUGINS"] = "health,ktor" }
  withType<KotlinCompile> { kotlinOptions.jvmTarget = "18" }
  withType<Test> { useJUnitPlatform() }
}

idea {
  module {
    // Not using += due to https://github.com/gradle/gradle/issues/8749
    sourceDirs =
        sourceDirs + file("build/generated/ksp/main/kotlin") // or tasks["kspKotlin"].destination
    testSourceDirs = testSourceDirs + file("build/generated/ksp/test/kotlin")
    generatedSourceDirs =
        generatedSourceDirs +
            file("build/generated/ksp/main/kotlin") +
            file("build/generated/ksp/test/kotlin")
  }
}

kotlin {
  sourceSets { all { languageSettings { enableLanguageFeature("ContextReceivers") } } }
  jvmToolchain { languageVersion.set(JavaLanguageVersion.of(19)) }
}
