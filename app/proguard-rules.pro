# Add project specific ProGuard rules here.

# Preserve useful crash line numbers while removing original source file names.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Preserve annotations and inner classes
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep Application, MainActivity & ViewModels
-keep class com.example.MainApplication { *; }
-keep class com.example.VirtusApplication { *; }
-keep class com.example.MainActivity { *; }
-keep class com.example.ui.** { *; }
-keep class com.example.service.** { *; }
-keep class com.example.util.** { *; }

# Keep data models used by Room, Firebase & Serializers
-keep class com.example.data.model.** { *; }
-keep class com.example.data.local.** { *; }
-keep class com.example.data.repository.** { *; }

# Room Database Rules (CRITICAL for R8 release builds)
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class * implements androidx.room.EntityDeletionOrUpdateAdapter { *; }
-keep class * implements androidx.room.EntityInsertionAdapter { *; }
-dontwarn androidx.room.paging.**

# Kotlinx Serialization & Navigation Compose
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keepclassmembers class * {
    ** Companion;
}
-keep class kotlinx.serialization.** { *; }

# Firebase & Google Play Services
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Coil Image Loader
-keep class coil.** { *; }

# Never emit verbose Android logging calls in release builds.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}
