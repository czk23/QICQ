plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.myqicq"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myqicq"
        minSdk = 24
        targetSdk = 34
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation (libs.android.sdk)
    implementation (libs.android.mail)
    implementation (libs.io.reactivex.rxjava2.rxjava3)
    implementation (libs.rxjava2.rxandroid)
    implementation (libs.gson)

    implementation (libs.okhttp)
    implementation (libs.okio)

    implementation(files("libs/BaiduLBS_Android.jar"))

    // https://github.com/bumptech/glide
    implementation (libs.glide)

    // https://blog.csdn.net/j5856004/article/details/87870639
    implementation (libs.android.mail)
    implementation (libs.android.activation)

    // https://mvnrepository.com/artifact/com.gjiazhe/wavesidebar
    implementation(files("libs/wavesidebar-1.3.aar"))


}