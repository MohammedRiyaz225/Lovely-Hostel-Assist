import java.util.Properties

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) {
        f.inputStream().use { load(it) }
    }
}
val customBaseUrl    = localProps.getProperty("BASE_URL") ?: "http://10.0.2.2:8000/"
val releaseBaseUrl   = localProps.getProperty("BASE_URL") ?: "https://hostelfix-api.onrender.com/"
val keystorePath     = localProps.getProperty("KEYSTORE_FILE") ?: ""
val keystorePass     = localProps.getProperty("KEYSTORE_PASSWORD") ?: System.getenv("KEYSTORE_PASSWORD") ?: "hostelfix123"
val keystoreAlias    = localProps.getProperty("KEY_ALIAS")         ?: System.getenv("KEY_ALIAS")         ?: "hostelfix"
val keystoreKeyPass  = localProps.getProperty("KEY_PASSWORD")      ?: System.getenv("KEY_PASSWORD")      ?: "hostelfix123"

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace  = "com.hostelfix"
    compileSdk = 35

    defaultConfig {
        applicationId  = "com.hostelfix"
        minSdk         = 24
        targetSdk      = 35
        versionCode    = 1
        versionName    = "1.0.0"

        // MAPS_API_KEY is read from local.properties via Secrets Gradle Plugin
        manifestPlaceholders["MAPS_API_KEY"] = ""
    }

    signingConfigs {
        create("release") {
            // Reads from local.properties (KEYSTORE_FILE, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD)
            // Falls back to env vars, then default values
            val ksFile = if (keystorePath.isNotEmpty()) file(keystorePath)
                         else project.rootProject.file("release-keystore.jks")
            if (ksFile.exists()) {
                storeFile     = ksFile
                storePassword = keystorePass
                keyAlias      = keystoreAlias
                keyPassword   = keystoreKeyPass
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            // Uses BASE_URL from local.properties if defined (e.g. BASE_URL=http://192.168.1.5:8000/), else defaults to http://10.0.2.2:8000/
            buildConfigField("String", "BASE_URL", "\"$customBaseUrl\"")
        }
        release {
            isMinifyEnabled   = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Use release signing if keystore exists, else fall back to debug signing
            val ksFile = if (keystorePath.isNotEmpty()) file(keystorePath)
                         else project.rootProject.file("release-keystore.jks")
            signingConfig = if (ksFile.exists()) signingConfigs.getByName("release")
                            else signingConfigs.getByName("debug")
            // Production URL read from local.properties BASE_URL, falls back to Render URL
            buildConfigField("String", "BASE_URL", "\"$releaseBaseUrl\"")
        }
    }

    buildFeatures {
        compose     = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)

    // Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.3")

    // Lifecycle & ViewModel
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.4")

    // Retrofit + OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Coil (image loading)
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Google Maps Compose
    implementation("com.google.maps.android:maps-compose:6.2.1")
    implementation("com.google.android.gms:play-services-maps:19.0.0")

    // Location (FusedLocationProvider)
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Firebase (FCM)
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")

    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

secrets {
    // local.properties key MAPS_API_KEY → automatically injected into BuildConfig & Manifest
    propertiesFileName        = "local.properties"
    defaultPropertiesFileName = "local.defaults.properties"
}
