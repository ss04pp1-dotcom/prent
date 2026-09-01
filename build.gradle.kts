plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.hilt.android) apply false
    // alias(libs.plugins.google.services) apply false - removed, using Supabase
    // alias(libs.plugins.firebase.crashlytics) apply false - removed
    // alias(libs.plugins.firebase.perf) apply false - removed
}
