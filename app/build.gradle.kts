plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.perf)
}

android {
    namespace = "livefootball.footballstreamning.fifaworldcup"
    compileSdk = 36

    defaultConfig {
        applicationId = "livefootball.footballstreamning.fifaworldcup"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

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
            // Note: Add your signingConfig here for Play Store releases
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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
    annotationProcessor(libs.glide.compiler)

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
    annotationProcessor(libs.hilt.compiler)

    // JSON Parsing (Gson)
    implementation(libs.gson)

    // Local Database (Room)
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
    implementation(libs.room.common)

    // Background Tasks (WorkManager)
    implementation(libs.work.runtime)
    implementation(libs.hilt.work)
    annotationProcessor(libs.hilt.work.compiler)

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


    implementation(files(*fileTree("libs").matching { include("*.jar") }.files.toTypedArray()))
    implementation(project(":newjustplayer"))
}
