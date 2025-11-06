plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
}

android {
    namespace = "com.duy842.student_application_project"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.duy842.student_application_project"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // Needed for instrumented tests
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // --- Core + Compose (using your version catalog + BOM) ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Compose tooling (debug only)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // --- DataStore ---
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // --- Room ---
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // --- Lifecycle ViewModel ---
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")

    // --- Coroutines (Android) ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // ============================
    // Unit tests (JVM) - src/test
    // ============================
    testImplementation(libs.junit) // from your catalog
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    // If you later need Flow assertions or mocks, you can add:
    // testImplementation("app.cash.turbine:turbine:1.1.0")
    // testImplementation("io.mockk:mockk:1.13.12")

    // ===================================
    // Instrumented tests - src/androidTest
    // ===================================
    androidTestImplementation(libs.androidx.junit)          // androidx.test.ext:junit
    androidTestImplementation(libs.androidx.espresso.core)  // Espresso core

    // Room testing helpers (for in-memory DB in androidTest)
    androidTestImplementation("androidx.room:room-testing:2.6.1")

    // Compose UI test (only if/when you write UI tests)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // AndroidX Test core + runner + rules + JUnit ext
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")

    androidTestImplementation("androidx.room:room-testing:2.6.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("io.mockk:mockk:1.13.12")





// (You can keep your existing Espresso/Compose test deps too)


}
