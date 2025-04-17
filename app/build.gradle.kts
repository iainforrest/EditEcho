plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.editecho"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.editecho"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.6.0"  // Updated to match Kotlin 2.0.x better
    }

    // Configure the Secrets Gradle Plugin
    secrets {
        defaultPropertiesFileName = "local.properties"
        properties {
            OPENAI_API_KEY {
                required = true
            }
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Compose UI dependencies
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.util)

    // Compose ViewTree classes - explicit implementation for FloatingBubbleService
    implementation("androidx.compose.ui:ui:1.6.0")
    implementation("androidx.compose.runtime:runtime:1.6.0")
    
    // Update lifecycle dependencies to latest versions
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-common:2.7.0")
    
    // Saved state components
    implementation(libs.savedstate)
    implementation(libs.savedstate.ktx)

    // Activity components with back handler support
    implementation(libs.activity)
    implementation(libs.activity.ktx)
    implementation(libs.activity.compose)

    implementation(libs.okhttp)

    // Network libraries for API calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // OkHttp for network requests
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    
    // Coroutines for asynchronous programming
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)

    debugImplementation(libs.compose.ui.tooling)
    implementation(platform(libs.compose.bom))
}