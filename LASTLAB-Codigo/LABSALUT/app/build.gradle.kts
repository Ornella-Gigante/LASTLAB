import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("com.google.secrets_gradle_plugin") version "0.5"
}

android {
    namespace = "es.ifp.labsalut"
    compileSdk = 34

    defaultConfig {
        applicationId = "es.ifp.labsalut"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Cargar desde secrets.properties
        val secretsPropertiesFile = rootProject.file("secrets.properties")
        if (secretsPropertiesFile.exists()) {
            val secretsProperties = Properties().apply {
                load(secretsPropertiesFile.inputStream())
            }
            val mapsApiKey: String = secretsProperties.getProperty("MAPS_API_KEY", "")
            val placesApiKey: String = secretsProperties.getProperty("PLACES_API_KEY", "")
            manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
            manifestPlaceholders["PLACES_API_KEY"] = placesApiKey
            resValue("string", "maps_api_key", mapsApiKey)
            resValue("string", "places_api_key", placesApiKey)
        } else {
            val mapsApiKey: String = System.getenv("MAPS_API_KEY") ?: ""
            val placesApiKey: String = System.getenv("PLACES_API_KEY") ?: ""
            manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
            manifestPlaceholders["PLACES_API_KEY"] = placesApiKey
            resValue("string", "maps_api_key", mapsApiKey)
            resValue("string", "places_api_key", placesApiKey)
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

    //Agregando bloque para el testeo

    testOptions {
        unitTests.apply {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.preference)
    implementation(libs.biometric)
    implementation(libs.security.crypto)
    implementation(libs.material.v1120)
    implementation(libs.places)
    implementation (libs.viewpager2)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation (libs.github.glide)
    implementation (libs.material.v150)
    implementation(libs.activity)
    implementation(libs.annotation)
    testImplementation(libs.testng)
    testImplementation(libs.androidx.fragment.testing)
    testImplementation(libs.androidx.fragment.testing)
    testImplementation(libs.ext.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // NUEVAS DEPENDENCIAS PARA TESTING AGREGADAS
    testImplementation(libs.junit)
    testImplementation(libs.androidx.test)
    testImplementation(libs.androidx.test.rules)
    testImplementation(libs.androidx.test.runner)
    testImplementation(libs.mockito.core)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.fragment.testing.v155)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)
    androidTestImplementation(libs.androidx.fragment.testing.v185)
    androidTestImplementation(libs.androidx.junit.v121)
    androidTestImplementation(libs.androidx.espresso.core.v361)
}