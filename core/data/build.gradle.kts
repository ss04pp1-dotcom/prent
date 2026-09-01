plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.parentalcare.core.data"
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
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.security.crypto)
    implementation(libs.kotlinx.serialization.json)

    implementation(project(":core:common"))
    implementation(project(":core:security"))
    // implementation(project(":core:firebase")) - removed, using Supabase
    
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    // implementation(libs.firebase.auth) - removed, using Supabase
    // implementation(libs.firebase.firestore) - removed, using Supabase
    // implementation(libs.firebase.storage) - removed, using Supabase
    // implementation(libs.firebase.appcheck.playintegrity) - removed
    
    // Supabase dependencies
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest)
    api(libs.supabase.gotrue)
    implementation(libs.supabase.storage)
    implementation(libs.supabase.realtime)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    
    implementation(libs.timber)
    implementation("com.google.android.gms:play-services-auth:21.2.0")
}
