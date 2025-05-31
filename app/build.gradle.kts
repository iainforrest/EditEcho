// app/build.gradle.kts

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services") version "4.4.2"
}

import java.util.Properties

// Load API keys from secrets.properties
val secretsFile = rootProject.file("secrets.properties")
val properties = Properties().apply {
    if (secretsFile.exists()) load(secretsFile.inputStream())
}

val openAiKey: String = properties.getProperty("OPENAI_API_KEY", "").also {
    if (it.isBlank()) logger.warn("⚠️  OPENAI_API_KEY is blank in secrets.properties")
}

val claudeKey: String = properties.getProperty("CLAUDE_API_KEY", "").also {
    if (it.isBlank()) logger.warn("⚠️  CLAUDE_API_KEY is blank in secrets.properties")
}

android {
    namespace   = "com.editecho"
    compileSdk  = 34

    defaultConfig {
        applicationId = "com.editecho"
        minSdk        = 31
        targetSdk     = 34
        versionCode   = 2
        versionName   = "0.2.0"

        buildConfigField(
            "String",
            "OPENAI_API_KEY",
            "\"$openAiKey\""
        )
        
        buildConfigField(
            "String",
            "CLAUDE_API_KEY",
            "\"$claudeKey\""
        )
    }

    // ─── Build Types ───────────────────────────────────────────────────────
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
            isMinifyEnabled = false
            
            // Add a build config field to identify debug builds
            buildConfigField("boolean", "IS_DEBUG_BUILD", "true")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            buildConfigField("boolean", "IS_DEBUG_BUILD", "false")
        }
    }

    // ─── Product Flavors ───────────────────────────────────────────────────
    flavorDimensions += "version"
    productFlavors {
        create("stable") {
            dimension = "version"
            // Uses base applicationId and versionName
            buildConfigField("String", "FLAVOR_NAME", "\"stable\"")
        }
        create("dev") {
            dimension = "version"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            
            // Increment version code for dev builds
            versionCode = (defaultConfig.versionCode ?: 1) + 1000
            
            buildConfigField("String", "FLAVOR_NAME", "\"dev\"")
        }
    }

    buildFeatures {
        compose     = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.12"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            pickFirsts += setOf(
                "/META-INF/AL2.0",
                "/META-INF/LGPL2.1"
            )
        }
    }

    // Kotlin JVM target must match Java target
    kotlinOptions {
        jvmTarget = "17"
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // ─── Firebase (version-aligned via BoM) ────────────────────────────────
    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    // Add other Firebase libs here, no version tags needed

    // ─── Core AndroidX & Compose ───────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    implementation(libs.compose.ui)
    implementation(libs.compose.runtime)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.activity.compose)

    // ─── Compose tooling & previews (debug only) ───────────────────────────
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.tooling.preview)

    // ─── Lifecycle & ViewModel ─────────────────────────────────────────────
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.savedstate)

    // ─── Jetpack DataStore ────────────────────────────────────────────────
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // ─── Dependency Injection (Hilt) ───────────────────────────────────────
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // ─── KotlinX JSON & Coroutines ────────────────────────────────────────
    implementation(libs.kotlinx.serialization.json)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // ─── Retrofit & OkHttp ────────────────────────────────────────────────
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("com.squareup.okhttp3:okhttp-sse:4.12.0")

    // ─── Test Dependencies ────────────────────────────────────────────────
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)

    // MockK
    testImplementation("io.mockk:mockk:1.13.10")
    androidTestImplementation("io.mockk:mockk-android:1.13.10")

    // Robolectric
    testImplementation("org.robolectric:robolectric:4.11.1")

    // Hilt Testing
    testImplementation("com.google.dagger:hilt-android-testing:2.50")
    kspTest("com.google.dagger:hilt-compiler:2.50")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.50")
    kspAndroidTest("com.google.dagger:hilt-compiler:2.50")
    
    // AndroidX Test Core
    testImplementation("androidx.test:core-ktx:1.5.0")

    // AndroidX Truth
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("com.google.truth:truth:1.1.5")
    androidTestImplementation("com.google.truth:truth:1.1.5")
}

