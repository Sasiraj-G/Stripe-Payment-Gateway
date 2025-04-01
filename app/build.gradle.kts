plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    kotlin("plugin.serialization") version "2.1.10"
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    id("com.apollographql.apollo3") version "3.8.0"
}


apollo {
    service("service") {
        packageName.set("com.example.paymentgateway")//your package name
        introspection {
            endpointUrl.set("https://staging1.flutterapps.io/api/graphql") //replace with your endpoint
            schemaFile.set(file("src/main/graphql/schema.graphqls")) //replace with the schema file location
        }
    }
}

kapt {
    correctErrorTypes = true
}
android {
    namespace = "com.example.paymentgateway"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.paymentgateway"
        minSdk = 26
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
    buildFeatures {
        //noinspection DataBindingWithoutKapt
        dataBinding = true
    }



}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.swiperefreshlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.google.android.material:material:1.12.0")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.1")  //2.8.7 -> 2.4.1
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
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.1")  //2.5.1 -> 2.4.1
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.4.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")


    //crop image

    implementation("com.soundcloud.android:android-crop:1.0.1@aar")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    

    //image compression
    implementation("id.zelory:compressor:3.0.1")

    //trips page cirlce profile
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // //Epoxy

    kapt ("com.airbnb.android:epoxy-processor:3.9.0")
    implementation ("com.airbnb.android:epoxy:3.9.0")
    implementation ("com.airbnb.android:epoxy-databinding:3.9.0")


    // Apollo GraphQL client
    implementation("com.apollographql.apollo3:apollo-runtime:3.8.0")

    //dagger
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")


    //google new place api
    implementation("com.google.android.libraries.places:places:3.5.0")

    //image zoom
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    implementation("com.airbnb.android:epoxy-paging:3.9.0")


    //veriff
    implementation("com.veriff:veriff-library:6.5.0")
    implementation ("com.squareup.moshi:moshi:1.14.0")
    implementation ("com.squareup.moshi:moshi-kotlin:1.14.0")
    implementation ("com.squareup.moshi:moshi-adapters:1.14.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    kapt ("com.squareup.moshi:moshi-kotlin-codegen:1.14.0")








}