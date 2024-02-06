plugins {
    id("com.android.application")
    id("kotlin-android")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
}

android {

    namespace = "dev.sanskar.transactions"

    compileSdk = 34

    defaultConfig {
        applicationId = "dev.sanskar.transactions"
        minSdk = 21
        targetSdk = 34
        versionCode = 19
        versionName = "0.8.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    // Android and Google
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("com.google.android.material:material:1.7.0-alpha01")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    implementation("androidx.navigation:navigation-fragment-ktx:2.4.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.4.2")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.fragment:fragment-ktx:1.4.1")
    implementation("com.google.firebase:firebase-crashlytics-ktx:18.2.9")
    implementation("com.google.firebase:firebase-analytics-ktx:20.1.2")
    implementation("androidx.work:work-runtime-ktx:2.7.1")

    // ViewModel (added to solve dependency issue)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    // Google Play
    implementation("com.google.android.play:core:1.10.3")
    implementation("com.google.android.play:core-ktx:1.8.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-compiler:2.48")

    // Room
    val room_version = "2.4.2"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    // Community
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.github.deano2390:MaterialShowcaseView:1.3.4")

    // Lottie
    implementation("com.airbnb.android:lottie:5.0.3")

    // Chrome Custom Tabs
    implementation("androidx.browser:browser:1.4.0")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")

}