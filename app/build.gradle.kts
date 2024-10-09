plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.cu"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.cu"
        minSdk = 22
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packagingOptions {
        resources {
            excludes += setOf("META-INF/NOTICE.md", "META-INF/LICENSE.md")
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
}

dependencies {
    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.material3.android)

    // Firebase BOM for consistent versions of Firebase dependencies
    implementation(enforcedPlatform("com.google.firebase:firebase-bom:32.0.0"))

    // Firebase dependencies without specifying versions
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")

    // CameraX for camera functionality
    implementation("androidx.camera:camera-core:1.3.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-video:1.3.0")
    implementation("androidx.camera:camera-view:1.5.0-alpha01")
    implementation("androidx.camera:camera-mlkit-vision:1.5.0-alpha01")
    implementation("androidx.camera:camera-extensions:1.5.0-alpha01")

    // ML Kit for barcode scanning
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    implementation("com.google.android.gms:play-services-mlkit-barcode-scanning:18.3.1")

    //zxing for QR code generation
    implementation ("com.google.zxing:core:3.5.0")

    // UI libraries
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.recyclerview)
    implementation(libs.circleimageview)
    implementation(libs.library)  // Progress Dialog

    // Glide for image loading
    implementation(libs.glide)

    // Kotlin extensions for Firebase
    implementation(libs.firebase.analytics.ktx)

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}