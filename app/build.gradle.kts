plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "dev.nexus.mobile"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.nexus.mobile"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}
