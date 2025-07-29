plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "be.buithg.etghaifgte"
    compileSdk = 35

    defaultConfig {
        applicationId = "xx.example.sample"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
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

    viewBinding.enable = true
}

dependencies {
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.volley)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation (libs.retrofit)
    // Конвертер JSON → Kotlin объекты через Gson
    implementation (libs.converter.gson)
    // Логирующий интерцептор для отладки HTTP
    implementation (libs.logging.interceptor)

    // Room
    val room_version = "2.7.2"
    implementation("androidx.room:room-runtime:$room_version")

    // Room compiler via KSP
    ksp("androidx.room:room-compiler:$room_version")

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.runtime.ktx)


    implementation("com.github.bumptech.glide:glide:4.16.0")

    val nav_version = "2.9.0"
    implementation("androidx.navigation:navigation-fragment:$nav_version")
    implementation("androidx.navigation:navigation-ui:$nav_version")

    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation ("com.squareup.okhttp3:okhttp:4.10.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.10.0")

}