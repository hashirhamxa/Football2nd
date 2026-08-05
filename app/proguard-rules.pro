# ============================
# General ProGuard Settings
# ============================
-dontwarn android.media.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn com.bumptech.glide.**
-dontwarn androidx.work.**
-dontwarn androidx.startup.**

# ============================
# Retrofit & Gson Keep Rules
# ============================
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*, Exceptions

# Keep Retrofit API interfaces
-keep interface livefootball.footballstreamning.fifaworldcup.network.ApiService {
    @retrofit2.http.* <methods>;
}
-keep interface livefootball.footballstreamning.fifaworldcup.network.ScoreApiService {
    @retrofit2.http.* <methods>;
}

# Keep models and preserve generic signatures
-keep class livefootball.footballstreamning.fifaworldcup.models.** { *; }

# Allow obfuscation/shrinking for Retrofit types but keep them enough for reflection
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Gson specific rules
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken

# OkHttp3 Platform Warnings
-dontwarn okhttp3.internal.platform.ConscryptPlatform
-dontwarn okhttp3.internal.platform.BouncyCastlePlatform
-dontwarn okhttp3.internal.platform.OpenJSSEPlatform

# ============================
# Glide Keep Rules
# ============================
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * implements com.bumptech.glide.module.AppGlideModule
-keep public class * extends com.bumptech.glide.request.BaseRequestOptions
-keep class com.bumptech.glide.Glide { *; }

# ============================
# Application & Project Classes
# ============================
# Adjust to match your current namespace
-keep class livefootball.footballstreamning.fifaworldcup.** { *; }

# ============================
# Room Persistence Library
# ============================
-keep class * extends androidx.room.RoomDatabase
-keep class livefootball.footballstreamning.fifaworldcup.database.** { *; }

# ============================
# Hilt / Dagger
# ============================
-keep class dagger.hilt.android.internal.** { *; }
-keep class * extends android.app.Application
-keep @dagger.hilt.android.HiltAndroidApp class *
-keep @dagger.hilt.android.lifecycle.HiltViewModel class *

# ============================
# Ads (AdMob & Unity)
# ============================
# AdMob
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.gms.common.** { *; }

# Unity Ads
-keep class com.unity3d.ads.** { *; }
-keep class com.unity3d.services.** { *; }

# ============================
# Media3 / ExoPlayer
# ============================
-keep class androidx.media3.exoplayer.** { *; }
-keep class androidx.media3.common.** { *; }
-keep class androidx.media3.datasource.** { *; }
-keep class androidx.media3.ui.** { *; }

# ============================
# Android Resources & Parcelable
# ============================
-keep class **.R$* { *; }
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# ============================
# WorkManager & Startup
# ============================
-keep class androidx.work.** { *; }
-keep class androidx.startup.** { *; }
