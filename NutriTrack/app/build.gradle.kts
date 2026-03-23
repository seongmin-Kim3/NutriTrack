plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.example.nutritrack"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.nutritrack"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
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
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))

    // Core
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")


    // Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    // CameraX
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")

// ML Kit - Barcode Scanning
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

// CameraX
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")

// ML Kit - Barcode Scanning
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

// Compose에서 권한 요청 도우미(간단하게 하려면 추천)
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // build.gradle.kts (Module:app) 파일의 dependencies { ... } 안에 추가

    // ✅ CameraX (카메라 화면을 띄우는 라이브러리)
    val camerax_version = "1.3.1" // 최신 안정화 버전
    implementation("androidx.camera:camera-camera2:$camerax_version")
    implementation("androidx.camera:camera-lifecycle:$camerax_version")
    implementation("androidx.camera:camera-view:$ camerax_version")

    // ✅ 구글 ML Kit 바코드 스캔 (바코드를 분석하는 AI API)
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    // ✅ 동시성 제어를 위한 Guava (CameraX 및 ML Kit 비동기 처리용)
    implementation("com.google.guava:guava:31.1-android")

}
