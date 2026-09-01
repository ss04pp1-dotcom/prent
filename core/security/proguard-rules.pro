# Keep all serializable pairing models
-keep class com.parentalcare.core.security.pairing.** { *; }
-keep class com.parentalcare.core.security.model.** { *; }
-keepclassmembers class com.parentalcare.core.security.** {
    @kotlinx.serialization.Serializable <fields>;
}
