import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.perf)
}
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

val apiToken = localProperties.getProperty("API_TOKEN") ?: "YOUR_API_TOKEN_HERE"

android {
    //livecricket.livecrickettv.cricketstreaming
    namespace = "livefootball.footballstreamning.fifaworldcup"
    compileSdk = 36

    defaultConfig {
        applicationId = "livefootball.footballstreamning.fifaworldcup"
        minSdk = 24
        targetSdk = 36
        versionCode = 4
        versionName = "1.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "API_TOKEN", "\"$apiToken\"")

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
    }

    buildTypes {
        release {
            // For closed testing: disable shrinking and obfuscation to avoid unexpected issues
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    // UI
    implementation(libs.activity.ktx)
    implementation(libs.fragment.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.recyclerview)
    implementation(libs.swiperefreshlayout)


    // Lifecycle
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)

    // Image Loading
    implementation(libs.glide.lib)
    kapt(libs.glide.compiler)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)

    // Networking (Retrofit)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Dependency Injection (Hilt)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // JSON Parsing (Gson)
    implementation(libs.gson)

    // Local Database (Room)
    implementation(libs.room.runtime)
    kapt(libs.room.compiler)
    implementation(libs.room.common)

    // Background Tasks (WorkManager)
    implementation(libs.work.runtime)
    implementation(libs.hilt.work)
    kapt(libs.hilt.work.compiler)

    // Shimmer
    implementation(libs.shimmer)

    //ads
    implementation(libs.play.services.ads)
    implementation(libs.unity.ads)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.perf)


    implementation(files("../newjustplayer/libs/lib-ui-release.aar"))
    implementation(files("../newjustplayer/libs/lib-exoplayer-release.aar"))
    implementation(files("../newjustplayer/libs/lib-extractor-release.aar"))
    implementation(files("../newjustplayer/libs/lib-decoder-av1-release.aar"))
    implementation(files("../newjustplayer/libs/lib-decoder-ffmpeg-release.aar"))
    implementation(files(*fileTree("libs").matching { include("*.jar") }.files.toTypedArray()))
    implementation(project(":newjustplayer"))
}
