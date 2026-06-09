# General ProGuard Rules
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*
-keepattributes SourceFile, LineNumberTable

# AndroidX / Lifecycle / ViewModel
-keep class androidx.lifecycle.ProcessLifecycleOwnerInitializer { *; }
-dontwarn androidx.lifecycle.**

# Room Persistence Library
-keep class * extends androidx.room.RoomDatabase {
    <init>();
}
-keep class bicodes.fifa.footballapp.data.** { *; }
-keep interface bicodes.fifa.footballapp.data.** { *; }
-dontwarn androidx.room.**

# Retrofit 2
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleAnnotations, RuntimeInvisibleParameterAnnotations
-keep interface bicodes.fifa.footballapp.api.** { *; }

# OkHttp 3 / Okio
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Gson
-keep class com.google.gson.** { *; }
-keep class bicodes.fifa.footballapp.model.** { *; }
-keepclassmembers class bicodes.fifa.footballapp.model.** {
    <fields>;
}
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken

# Glide
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public class * extends com.bumptech.glide.module.LibraryGlideModule
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl { *; }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-dontwarn com.bumptech.glide.**

# Support for Kotlin (even if mainly Java project, libraries might use it)
-dontwarn kotlin.**
-keep class kotlin.** { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}

# Keep your UI classes (Activities, Fragments)
-keep class bicodes.fifa.footballapp.ui.** { *; }
