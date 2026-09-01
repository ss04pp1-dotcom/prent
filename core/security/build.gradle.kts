plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
}
android {
    namespace = "com.parentalcare.core.security"
    compileSdk = 35
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}
dependencies {
    implementation(project(":core:common"))
    implementation(libs.androidx.security.crypto)
    implementation(libs.kotlinx.serialization.json)
    
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    
    implementation(libs.timber)
}
