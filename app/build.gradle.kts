plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") 
}

// Import Properties for reading secrets.properties
import java.util.Properties

// Load the real API key from secrets.properties and inject into BuildConfig
val secretsFile = rootProject.file("secrets.properties")
val openAiKey: String = Properties().apply {
    if (secretsFile.exists()) load(secretsFile.inputStream())
}.getProperty("OPENAI_API_KEY", "").also {
    if (it.isBlank()) logger.warn("⚠️ OPENAI_API_KEY is blank in secrets.properties")
}

android {
    namespace = "com.example.editecho"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.editecho"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Inject the API key into BuildConfig
        buildConfigField("String", "OPENAI_API_KEY", "\"$openAiKey\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
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
    
    // Update lifecycle dependencies to latest versions
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-common:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    
    // Saved state components
    implementation(libs.savedstate)
    implementation(libs.savedstate.ktx)

    // Activity components with back handler support
    implementation(libs.activity)
    implementation(libs.activity.ktx)
    implementation(libs.activity.compose)

    // Network libraries for API calls
    implementation("com.aallam.openai:openai-client:3.6.0")  // Downgrade to a more stable version
    implementation("io.github.aakira:napier:2.6.1")   // logger for the client
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.squareup.okio:okio:3.9.0")
    
    // Ktor dependencies for OpenAI client
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-android:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    
    // Retrofit for Whisper API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0") // for plain text responses
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    
    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.4")
    debugImplementation(libs.compose.ui.test.manifest)
}