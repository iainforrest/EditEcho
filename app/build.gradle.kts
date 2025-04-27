// app/build.gradle.kts

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.kapt")
}

import java.util.Properties

// Load OPENAI_API_KEY from secrets.properties
val secretsFile = rootProject.file("secrets.properties")
val openAiKey: String = Properties().apply {
    if (secretsFile.exists()) load(secretsFile.inputStream())
}.getProperty("OPENAI_API_KEY", "").also {
    if (it.isBlank()) logger.warn("⚠️  OPENAI_API_KEY is blank in secrets.properties")
}

android {
    namespace   = "com.example.editecho"
    compileSdk  = 34

    defaultConfig {
        applicationId = "com.example.editecho"
        minSdk        = 31
        targetSdk     = 34
        versionCode   = 1
        versionName   = "0.1.0"

        buildConfigField(
            "String",
            "OPENAI_API_KEY",
            "\"$openAiKey\""
        )
    }

    buildFeatures {
        compose     = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    // Kotlin JVM target must match Java target
    kotlinOptions {
        jvmTarget = "21"
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    // ─── Core AndroidX & Compose ───────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    implementation(libs.compose.ui)
    implementation(libs.compose.runtime)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.activity.compose)

    // Compose tooling & previews
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.tooling.preview)

    // ─── Lifecycle & ViewModel ────────────────────────────────────────────
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.savedstate)

    // ─── DataStore Preferences ─────────────────────────────────────────────
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // ─── Hilt / Dagger ─────────────────────────────────────────────────────
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // ─── Networking & JSON ────────────────────────────────────────────────
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.okhttp.sse)
    implementation(libs.retrofit)
    implementation(libs.retrofit.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}
