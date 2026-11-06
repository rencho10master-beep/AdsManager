plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
}

android {
    namespace = "com.ads.manager"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.github.LionsLLC"
                artifactId = "adsmanager"
                version = "1.0.3"
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // SDP and SSP for responsive dimensions
    implementation(libs.sdp.android)
    implementation(libs.ssp.android)

    // Android Lifecycle Process
    implementation(libs.androidx.lifecycle.process)

    // Google Mobile Ads (Next-Gen SDK)
    implementation(libs.ads.mobile.sdk)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
