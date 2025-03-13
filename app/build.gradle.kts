plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    kotlin("plugin.serialization") version "2.1.10"
}

android {
    namespace = "com.example.paymentgateway"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.paymentgateway"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    }
    buildFeatures {
        viewBinding = true
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("com.stripe:stripe-android:20.1.0")

 //paypal payment
    implementation(libs.paypal.web.payments)
    implementation(libs.android.networking)
    implementation(libs.okhttp)
    implementation("com.google.code.gson:gson:2.10.1")


    // Retrofit for network calls
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")

    // Coil for image loading
    implementation(libs.coil)
    // ViewModel and LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")


    //crop image

    implementation("com.soundcloud.android:android-crop:1.0.1@aar")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    

    //image compression
    implementation("id.zelory:compressor:3.0.1")


}