plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.testing"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.testing"
        minSdk = 29
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation("androidx.compose.runtime:runtime-saveable:1.5.4")
    implementation("androidx.compose.material3:material3:1.0.1")
    implementation("androidx.compose.ui:ui:1.3.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.3.0")

    // DataStore (For saving theme preference across app restarts)
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    implementation("androidx.navigation:navigation-compose:2.7.5")

    // CameraX
    implementation("androidx.camera:camera-core:1.3.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")

    // Přidáno: Material Icons Extended
    implementation("androidx.compose.material:material-icons-extended:1.3.1")

    // ML Kit pro Image Labeling
    implementation("com.google.mlkit:image-labeling:17.0.7")

    implementation("com.google.mlkit:object-detection:17.0.2")

    // To recognize Latin script
    implementation("com.google.mlkit:text-recognition:16.0.1")

    // To recognize Chinese script
    implementation("com.google.mlkit:text-recognition-chinese:16.0.1")

    // To recognize Devanagari script
    implementation("com.google.mlkit:text-recognition-devanagari:16.0.1")

    // To recognize Japanese script
    implementation("com.google.mlkit:text-recognition-japanese:16.0.1")

    // To recognize Korean script
    implementation("com.google.mlkit:text-recognition-korean:16.0.1")
    implementation("com.google.mlkit:vision-common:17.3.0")

    // Coil
    implementation("io.coil-kt:coil-compose:2.2.2")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}


