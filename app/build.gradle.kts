// build.gradle.kts (Module: App)

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    // Hilt KSP/Compiler setup
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.neon.peggame"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.neon.peggame"
        minSdk = 26 // Min SDK 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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

    // Compose BOM - Use the latest stable version
    // Replace 2024.00.00 with the actual latest stable version (e.g., 2025.01.00)
    val composeBom = platform("androidx.compose:compose-bom:2024.00.00") // Placeholder
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Core Android & Compose
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.runtime.livedata)

    // Hilt (Dependency Injection)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    kapt(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Room (Database for Scores/Achievements) - Using simple KTX for now
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    // Accompanist (for Shaders/Effects - if needed, otherwise rely on built-in Compose)
    // Note: Shaders are now moving to androidx.compose.ui.graphics.shaders
    // For initial setup, we skip the Accompanist dependency unless an advanced effect is specifically required.
}
