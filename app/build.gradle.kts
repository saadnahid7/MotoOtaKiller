import java.util.Properties

plugins {
    id("com.android.application")
}

// Release signing comes from a git-ignored file; without it the release build is unsigned.
val signing: Properties? = rootProject.file(".local/signing.properties").takeIf { it.exists() }?.let { f ->
    Properties().also { p -> f.inputStream().use { p.load(it) } }
}

android {
    namespace  = "com.droidrooter.otakiller"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.droidrooter.otakiller"
        minSdk        = 26
        targetSdk     = 36
        versionCode   = 1
        versionName   = "1.0"
    }

    signingConfigs {
        if (signing != null) create("release") {
            storeFile     = rootProject.file(signing.getProperty("storeFile"))
            storePassword = signing.getProperty("storePassword")
            keyAlias      = signing.getProperty("keyAlias")
            keyPassword   = signing.getProperty("keyPassword")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (signing != null) signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    sourceSets["main"].resources.srcDir("src/main/resources")
    sourceSets["main"].java.srcDirs("src/main/kotlin")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    compileOnly("io.github.libxposed:api:102.0.0")
}
