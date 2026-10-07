import java.io.File

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "ru.randomwords"
    compileSdk = 36

    defaultConfig {
        applicationId = "ru.randomwords"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "1.1.0"
    }

    signingConfigs {
        val keystorePath = System.getenv("RELEASE_KEYSTORE_PATH")
        val keystorePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
        val keyAlias = System.getenv("RELEASE_KEY_ALIAS")
        val keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
        if (!keystorePath.isNullOrBlank() && File(keystorePath).exists() &&
            !keystorePassword.isNullOrBlank() && !keyAlias.isNullOrBlank() && !keyPassword.isNullOrBlank()) {
            create("release") {
                storeFile = File(keystorePath)
                storePassword = keystorePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            }
        }
    }

    buildTypes {
        getByName("release") {
            val hasReleaseSigning = signingConfigs.findByName("release") != null
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}
dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
}
