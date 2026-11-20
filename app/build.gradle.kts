import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val localSigning = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val ksFile = System.getenv("ANDROID_KEYSTORE_FILE")?.takeIf { it.isNotBlank() } ?: localSigning.getProperty("storeFile")
val ksPassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")?.takeIf { it.isNotBlank() } ?: localSigning.getProperty("storePassword")
val ksAlias = System.getenv("ANDROID_KEY_ALIAS")?.takeIf { it.isNotBlank() } ?: localSigning.getProperty("keyAlias")
val ksKeyPassword = System.getenv("ANDROID_KEY_PASSWORD")?.takeIf { it.isNotBlank() } ?: localSigning.getProperty("keyPassword")
val hasReleaseSigning = listOf(ksFile, ksPassword, ksAlias, ksKeyPassword).all { !it.isNullOrBlank() }

android {
    compileSdkVersion(33)

    defaultConfig {
        applicationId = "com.furuarc"
        minSdkVersion(24)
        targetSdkVersion(34)
        versionCode = 101
        versionName = "1.01"
    }

    // Release signing credentials are never stored in the repository. They are read from environment
    // variables (CI) or from an untracked `keystore.properties` in the project root (local builds).
    // Without them, `assembleRelease` produces an unsigned APK; debug builds are not affected.
    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(ksFile!!)
                storePassword = ksPassword
                keyAlias = ksAlias
                keyPassword = ksKeyPassword
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            if (hasReleaseSigning) signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.1.1" }
}

dependencies {
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.activity:activity-compose:1.4.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.4.1")
    implementation("androidx.documentfile:documentfile:1.0.1")

    implementation("androidx.compose.ui:ui:1.1.1")
    implementation("androidx.compose.material:material:1.1.1")
    implementation("androidx.compose.material:material-icons-core:1.1.1")
}

// Diagnostics: run `./gradlew printKotlinCompiler` to see which Kotlin compiler version is used
tasks.register("printKotlinCompiler") {
    doLast {
        project.configurations.matching { it.name.contains("kotlinCompiler", ignoreCase = true) }.forEach { c ->
            println("== ${c.name}")
            try { c.files.forEach { println("   ${it.name}") } } catch (e: Exception) { println("   (failed to resolve: ${e.message})") }
        }
    }
}

// Diagnostics: shown in the build log so you can see whether the latest gradle.properties was read
println(
    "[FuruArc] kotlin.compiler.execution.strategy=" + project.findProperty("kotlin.compiler.execution.strategy") +
        " | org.gradle.jvmargs=" + project.findProperty("org.gradle.jvmargs"),
)
