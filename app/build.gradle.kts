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
        minSdk = 24
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.material3.android)
    implementation(libs.filament.android)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.androidx.media3.common)
    implementation(libs.firebase.firestore.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.core)
    implementation(libs.android.mail)
    implementation(libs.android.activation)
    implementation(libs.zxing.android.embedded)
    implementation(libs.androidx.cardview)

    implementation(libs.firebase.database)
    implementation(libs.firebase.ui.database)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.recyclerview)
    implementation(libs.glide)

    //Circular image Library
    implementation(libs.circleimageview)

    //progress Dialog library
    implementation(libs.library)
    implementation("com.google.zxing:core:3.4.1")
    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("com.sun.mail:android-activation:1.6.7")

    // Firebase Firestore
    implementation(libs.firebase.firestore.ktx)

    // ZXing for QR code generation
    implementation(libs.core)

    // Glide for image loading
    implementation(libs.glide)

    // Kotlin extensions for Firebase
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.google.firebase.storage.ktx)
}