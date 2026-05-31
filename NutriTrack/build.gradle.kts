plugins {
    id("com.android.application") version "9.2.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.kapt") version "2.0.21" apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
    alias(libs.plugins.kotlin.compose) apply false
    // build.gradle.kts (대략 7-8행 부근)
    val geminiApiKey = "AQ.Ab8RN6Kx0N3NqYeovFwmtMhVhAzctp9bqfDq4_FP2YTx3o8viA"
    val foodSafetyApiKey = "e5f918dbd90f4b48a7ee"
}
