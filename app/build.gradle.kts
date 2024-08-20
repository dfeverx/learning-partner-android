plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.dagger.hilt.android)
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {

    namespace = "app.dfeverx.ninaiva"
    compileSdk = 34

    defaultConfig {
        applicationId = "app.dfeverx.ninaiva"
        minSdk = 25
        targetSdk = 34
        versionCode = 20
        versionName = "0.20"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] =
                    "$projectDir/schemas"
            }
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
        debug {
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
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.compose.icons)

//  adaptive ui
    implementation(libs.androidx.compose.adaptive)
    implementation(libs.androidx.compose.adaptive.layout)
    implementation(libs.androidx.compose.adaptive.navigation)
//    implementation(libs.androidx.compose.adaptive.navigation.suite)

//    accompanist
    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.pager.indicators)
    implementation(libs.accompanist.permissions)

//    shimmer
    implementation(libs.compose.shimmer)
//    markdown
    implementation(libs.compose.markdown)

//  coil
    implementation(libs.coil.compose)

//    lottie
    implementation(libs.lottie.compose)

//  pdf
    implementation(libs.pdf.viewer)

//  hilt
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.dagger.hilt)

    ksp(libs.androidx.hilt.compiler)
    ksp(libs.dagger.compiler)

//   room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.paging)
    implementation(libs.androidx.room.ktx)
    annotationProcessor(libs.androidx.room.compiler)
    ksp(
        libs.androidx.room.compiler
    )

//  gson
    implementation(libs.google.gson)

//    time formatting
    implementation(libs.android.prettytime)


//  lifecycle
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.lifecycle.viewmodel)

//   play
    implementation(libs.google.play.document.scanner)
    implementation(libs.google.play.text.recognition)
    implementation(libs.google.play.services.auth)
    implementation(libs.google.play.update)
    implementation(libs.google.play.update.ktx)
    implementation(libs.google.play.review)
    implementation(libs.google.play.review.ktx)
    implementation(libs.google.play.ads)
    implementation(libs.google.play.billing)
    implementation(libs.google.play.billing.ktx)

//    firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.functions)
    implementation(libs.firebase.remoteconfig)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.appcheck)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.inappmessaging)
    implementation(libs.firebase.inappmessaging.display)
    implementation(libs.firebase.messaging.ktx)

//    data store
    implementation(libs.androidx.datastore)

//  testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

