import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.alantech.boardgame"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.alantech.boardgame"
        minSdk = 25
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    val apiKey = getEnv("API_KEY")

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"http://localhost:8000/\"")
            buildConfigField("String", "API_KEY", "\"$apiKey\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "BASE_URL", "\"http://103.78.3.184:8000/\"")
            buildConfigField("String", "API_KEY", "$apiKey\"")
            signingConfig = signingConfigs.getByName("debug")
        }
    }



    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.add("-Xcontext-parameters")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation("androidx.navigation:navigation-compose:2.9.8")
    implementation("androidx.compose.material:material-icons-extended:1.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)


    implementation("io.coil-kt:coil-compose:2.7.0")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0")

    // OkHttp (for logging and networking layer)
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    val roomVersion = "2.8.4" // Use the latest stable version

    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion") // Coroutines support for Room
    ksp("androidx.room:room-compiler:$roomVersion") // Or use KSP if configured
    implementation("com.google.code.gson:gson:2.14.0")


    implementation("com.airbnb.android:lottie-compose:6.7.1")
    implementation("androidx.datastore:datastore-preferences:1.1.2")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.2")
    implementation("androidx.viewpager2:viewpager2:1.1.0")

    // Firebase SDK
    implementation(platform("com.google.firebase:firebase-bom:34.17.0"))
    implementation("com.google.firebase:firebase-analytics")
}

fun getEnv(key: String, default: String = ""): String {
    val envFile = rootProject.file(".env")
    if (envFile.exists()) {
        val properties = Properties()
        envFile.inputStream().use { properties.load(it) }
        val value = properties.getProperty(key)
        if (!value.isNullOrBlank()) {
            // Strips surrounding quotes if present in the .env file
            return value.trim().removeSurrounding("\"").removeSurrounding("'")
        }
    }
    // Fallback for CI/CD pipelines (e.g., GitHub Actions, Bitrise)
    return System.getenv(key) ?: default
}