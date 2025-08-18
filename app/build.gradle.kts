plugins {
    alias(libs.plugins.android.application)  // com.android.application
    alias(libs.plugins.kotlin.android)       // org.jetbrains.kotlin.android
    alias(libs.plugins.kotlin.compose)       // org.jetbrains.kotlin.plugin.compose
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.bankingsystem.mobile"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.bankingsystem.mobile"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8080/\"")
        buildConfigField("String", "SUPPORT_EMAIL", "\"support@bankapp.com\"")
        buildConfigField("boolean", "ENABLE_LOGGING", "true")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    kotlinOptions {
        freeCompilerArgs = listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}
dependencies {
    // Android core dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Compose UI dependencies
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.androidx.ui.text.google.fonts)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Material design dependencies
    implementation(libs.material)
    implementation(libs.material.icons.extended)

    // Network and data storage dependencies
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.biometric)

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Navigation dependencies
    implementation(libs.androidx.navigation.compose)

    // Image loading dependencies
    implementation(libs.coil.compose)

    // Face detection dependencies
    implementation(libs.face.detection)

    // --- Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.converter.moshi)
    implementation(libs.moshi.kotlin)
}

kapt { correctErrorTypes = true }
