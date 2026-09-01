# ============================================================================
# Parent app ProGuard / R8 rules
# ============================================================================

# --- Keep the Application class (Hilt-generated entry point) ---
-keep class com.parentalcare.parent.ParentApplication { *; }
-keep class com.parentalcare.parent.MainActivity { *; }
-keep class com.parentalcare.parent.fcm.ParentFcmService { *; }

# --- Hilt ---
-keep,allowobfuscation,allowshrinking class kotlin.Metadata
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.lifecycle.HiltViewModelFactory$ViewModelComponentBuilderEntryPoint { *; }

# --- Supabase (supabase-kt) ---
-keep class com.supabase.** { *; }
-dontwarn com.supabase.**
-keep class com.supabase.realtime.** { *; }
-keep class com.supabase.storage.** { *; }
-keep class com.supabase.auth.** { *; }
-keep class com.supabase.postgrest.** { *; }

# --- kotlinx.serialization ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.parentalcare.**$$serializer { *; }
-keepclassmembers class com.parentalcare.** {
    *** Companion;
}
-keepclasseswithmembers class com.parentalcare.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- ZXing (QR) ---
-keep class com.google.zxing.** { *; }
-keep class com.journeyapps.barcodescanner.** { *; }
-dontwarn com.google.zxing.**

# --- Coil (image loading) ---
-dontwarn coil.**

# --- Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# --- Compose / Kotlin reflect (needed by Compose runtime) ---
-keep class androidx.compose.** { *; }
-keep class kotlin.reflect.** { *; }
-dontwarn androidx.compose.**

# --- Parcelize ---
-keep @kotlinx.parcelize.RawValue class *
-keepnames class * @kotlinx.parcelize.RawValue
-keep class com.parentalcare.core.** { *; }
-keep class com.parentalcare.parent.** { *; }

# --- Strip logging in release ---
-assumenosideeffects class timber.log.Timber {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
}
# NEVER strip Timber.e() — keep error logs for diagnostics.

# Update Supabase rules for io.github.jan.supabase
-keep class io.github.jan.supabase.** { *; }
-dontwarn io.github.jan.supabase.**
