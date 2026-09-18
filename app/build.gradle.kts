plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.dk.together"
    compileSdk = 37
    compileSdkMinor = 0

    defaultConfig {
        applicationId = "com.dk.evelune"
        minSdk = 26
        targetSdk = 36
        versionCode = 9
        versionName = "0.9.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Stable non-production key used only for direct/sideload development builds.
    // Keep this signing identity unchanged and increment versionCode for every shipped APK so
    // Android can install future Evelune builds as updates instead of separate/rejected apps.
    signingConfigs {
        create("eveluneDevUpdate") {
            storeFile = file("signing/evelune-dev-update.jks")
            storePassword = "eveluneDev2026"
            keyAlias = "evelune_dev"
            keyPassword = "eveluneDev2026"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("eveluneDevUpdate")
        }
        release {
            signingConfig = signingConfigs.getByName("eveluneDevUpdate")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.08.00")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")

    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
}
