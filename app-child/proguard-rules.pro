# ============================================================================
# Child app ProGuard / R8 rules
# ============================================================================

-keep class com.parentalcare.child.ChildApplication { *; }
-keep class com.parentalcare.child.MainActivity { *; }
-keep class com.parentalcare.child.fcm.ChildFcmService { *; }
-keep class com.parentalcare.child.service.** { *; }
-keep class com.parentalcare.child.mediaprojection.** { *; }
-keep class com.parentalcare.child.pipeline.** { *; }

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
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.parentalcare.**$$serializer { *; }
-keepclassmembers class com.parentalcare.** { *** Companion; }
-keepclasseswithmembers class com.parentalcare.** { kotlinx.serialization.KSerializer serializer(...); }

# --- ZXing ---
-keep class com.google.zxing.** { *; }
-keep class com.journeyapps.barcodescanner.** { *; }
-dontwarn com.google.zxing.**

# --- Coil ---
-dontwarn coil.**

# --- Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# --- Compose ---
-keep class androidx.compose.** { *; }
-keep class kotlin.reflect.** { *; }
-dontwarn androidx.compose.**

# --- Parcelize ---
-keep @kotlinx.parcelize.RawValue class *

-keep class com.parentalcare.core.** { *; }
-keep class com.parentalcare.child.** { *; }

# --- Strip logging in release (keep errors) ---
-assumenosideeffects class timber.log.Timber {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
}

# Update Supabase rules for io.github.jan.supabase
-keep class io.github.jan.supabase.** { *; }
-dontwarn io.github.jan.supabase.**
