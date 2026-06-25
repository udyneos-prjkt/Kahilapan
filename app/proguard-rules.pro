# app/proguard-rules.pro

# ---- Data Classes ----
-keep class com.dinsoft.notes.data.** { *; }

# ---- Room Database ----
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# ---- ViewModel ----
-keep class * extends androidx.lifecycle.ViewModel { *; }

# ---- Gson (Backup/Restore) ----
-keep class com.google.gson.** { *; }
-keepclassmembers class com.dinsoft.notes.data.Note { <fields>; }

# ---- Kotlin ----
-keepattributes *Annotation*
-keep class kotlin.Metadata { *; }

# ---- Compose ----
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# ---- Remove Logging ----
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# ---- Keep R Classes ----
-keepclassmembers class **.R$* {
    public static <fields>;
}