plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.mobile.evocasa"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mobile.evocasa"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("com.google.firebase:firebase-bom:33.1.2")
    implementation ("com.google.firebase:firebase-firestore:25.1.4")
    implementation ("com.google.firebase:firebase-auth:23.2.1")
    implementation ("androidx.credentials:credentials:1.5.0")
    implementation ("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation ("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation ("at.favre.lib:bcrypt:0.9.0")



    implementation ("me.relex:circleindicator:2.1.6")
    implementation ("com.google.android.material:material:1.9.0")
}
