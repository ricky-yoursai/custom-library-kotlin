plugins {
    alias(libs.plugins.android.library)
    id("maven-publish") // JIT_PACK@ enable Maven publishing for JitPack
}

group = (project.findProperty("jitpack.group") as String?) ?: "com.github.ricky-yoursai" // JIT_PACK@ set to your GitHub user/org
version = (project.findProperty("jitpack.version") as String?) ?: "0.1.0" // JIT_PACK@ fallback version for local builds

val reactNativeVersion = (project.findProperty("reactNativeVersion") as String?) ?: "+" // JIT_PACK@ align with your RN app if needed

android {
    namespace = "com.yoursai.library"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

    publishing { // JIT_PACK@ publish release variant + sources for JitPack
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    compileOnly("com.facebook.react:react-android:$reactNativeVersion") // JIT_PACK@ React Native view manager APIs
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

afterEvaluate {
    publishing { // JIT_PACK@ create Maven publication consumed by JitPack
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
            }
        }
    }
}
