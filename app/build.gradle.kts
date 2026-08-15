plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.formaui.fintechuikit"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "dev.formaui.fintechuikit"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        // minSdk is 24, but java.time only exists from API 26. Desugaring backports it so
        // transaction dates can use LocalDate instead of Calendar.
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Match the compiler to the overridden KGP version (see root build.gradle.kts).
    add("kotlinCompilerClasspath", libs.kotlin.compiler.embeddable)

    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    // Deprecated and frozen at 1.7.8 — see the note in libs.versions.toml.
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.formaui.core)
    implementation(libs.formaui.components)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    // Supplies the @Preview *renderer*. ui-tooling-preview above is only the annotation.
    debugImplementation(libs.androidx.compose.ui.tooling)
}