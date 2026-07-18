plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.school.browser"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.school.browser"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = false // 安全要求：关闭 debuggable
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // AndroidX 核心
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-ktx:1.8.1")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.android.material:material:1.10.0")

    // Kotlin 序列化 (JSON)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Kotlin 协程
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // OkHttp (HTTP/HTTPS 策略拉取)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // SMB 协议 (jcifs-ng)
    implementation("eu.agno3.jcifs:jcifs-ng:2.1.10")

    // AndroidX Security (EncryptedSharedPreferences)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // BCrypt 密码哈希
    implementation("at.favre.lib:bcrypt:0.10.2")

    // ZXing (二维码生成 - 密码恢复用)
    implementation("com.google.zxing:core:3.5.3")

    // 测试
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

// 禁用所有 Lint 任务（解决 lintVitalAnalyzeDebug 内部崩溃问题）
tasks.configureEach {
    if (name.contains("lint", ignoreCase = true)) {
        enabled = false
    }
}
