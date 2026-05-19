import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinxSerialization)
}

val releaseKeystoreFile = providers.gradleProperty("ANDROID_RELEASE_KEYSTORE_FILE").orNull
val releaseKeystorePassword = providers.gradleProperty("ANDROID_RELEASE_KEYSTORE_PASSWORD").orNull
val releaseKeyAlias = providers.gradleProperty("ANDROID_RELEASE_KEY_ALIAS").orNull
val releaseKeyPassword = providers.gradleProperty("ANDROID_RELEASE_KEY_PASSWORD").orNull
val hasReleaseSigning = listOf(
    releaseKeystoreFile,
    releaseKeystorePassword,
    releaseKeyAlias,
    releaseKeyPassword
).all { !it.isNullOrBlank() }

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            binaryOption("bundleId", "ovo.sypw.kmp.examsystem")
        }
    }

    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            // Android平台特定的Ktor客户端
            implementation(libs.ktor.client.android)
            // Koin Android支持
            implementation(libs.koin.android)
            implementation(libs.mediation.sdk)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.animation)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.material.icons.extended)
            // 跨平台返回键处理
            implementation(libs.ui.backhandler)
            // KMP ViewModel
            implementation(libs.kmp.viewmodel)
            implementation(libs.kmp.viewmodel.compose)

            // Ktor网络请求核心库
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            // Koin依赖注入
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.kmp.viewmodel.koin.compose)
            // 图片加载库
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            // FileKit - 跨平台文件操作库
            implementation(libs.filekit.dialogs)
            implementation(libs.filekit.dialogs.compose)
            // kotlinx-datetime - 跨平台日期时间处理
            implementation(libs.kotlinx.datetime)

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            // Desktop平台特定的Ktor客户端
            implementation(libs.ktor.client.okhttp)
        }

        iosMain.dependencies {
            // iOS平台特定的Ktor客户端
            implementation(libs.ktor.client.darwin)
        }

    }
}

android {
    namespace = "ovo.sypw.kmp.examsystem"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    //noinspection GradleDependency

    defaultConfig {
        applicationId = "ovo.sypw.kmp.examsystem"
        minSdk = libs.versions.android.minSdk.get().toInt()
        //noinspection OldTargetApi
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 233
        versionName = "ovO2.33"
        buildConfigField(
            "String",
            "PANGLE_APP_ID",
            "\"${providers.gradleProperty("PANGLE_APP_ID").orElse("").get()}\""
        )
        buildConfigField(
            "String",
            "PANGLE_SPLASH_AD_ID",
            "\"${providers.gradleProperty("PANGLE_SPLASH_AD_ID").orElse("").get()}\""
        )
    }
    buildFeatures {
        buildConfig = true
    }
    signingConfigs {
        create("release") {
            if (hasReleaseSigning) {
                storeFile = file(releaseKeystoreFile!!)
                storePassword = releaseKeystorePassword!!
                keyAlias = releaseKeyAlias!!
                keyPassword = releaseKeyPassword!!
            }
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = if (hasReleaseSigning) signingConfigs.getByName("release") else signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    
    lint {
        disable += "NullSafeMutableLiveData"
        abortOnError = false
    }
}

dependencies {
    debugImplementation(compose.uiTooling)

}

compose.desktop {
    application {
        mainClass = "ovo.sypw.kmp.examsystem.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ovo.sypw.kmp.examsystem"
            packageVersion = "1.0.0"
        }
    }
}
