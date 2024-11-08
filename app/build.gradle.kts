// build.gradle (App)

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    id("com.google.gms.google-services")  // Google services
}

android {
    namespace = "tfg.azafatasapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "tfg.azafatasapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"  // La versión debe ser la correcta para tu Compose
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Firebase Dependencies
    implementation(platform(libs.firebase.bom))  // Firebase Bill of Materials
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.google.firebase.firestore.ktx)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.coil.compose.v230)

    // Jetpack Compose
    implementation(libs.ui)  // Versión de Compose
    implementation(libs.material3)  // Material3
    implementation(libs.androidx.material)  // Material (si es necesario)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.ui.tooling.preview)
    implementation(libs.coil.compose)  // Para imágenes en Compose
    implementation (libs.androidx.material3.v100)// para poder usar el elevation
    implementation (libs.coil.compose.v230)  // Dependencia para cargar fotos por url
    implementation (libs.firebase.storage) // Dependecia para usar el Storage
    implementation (libs.firebase.auth)
    implementation (libs.firebase.firestore)




    // Jetpack Components
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Otros
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.ui.test.android)
    implementation(libs.androidx.room.ktx)

    // Test dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
