import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

/** Upload-key settings from keystore.properties, else from RELEASE_* environment variables (CI). */
val releaseSigning: Map<String, String>?
    get() {
        val file = rootProject.file("keystore.properties")
        if (file.exists()) {
            val p = Properties().apply { file.inputStream().use { load(it) } }
            return listOf("storeFile", "storePassword", "keyAlias", "keyPassword")
                .associateWith { p.getProperty(it) ?: error("keystore.properties is missing $it") }
        }
        // Trimmed: values pasted into GitHub secrets easily pick up a stray space or line break.
        val env = mapOf(
            "storeFile" to System.getenv("RELEASE_STORE_FILE")?.trim(),
            "storePassword" to System.getenv("RELEASE_STORE_PASSWORD")?.trim(),
            "keyAlias" to System.getenv("RELEASE_KEY_ALIAS")?.trim(),
            "keyPassword" to System.getenv("RELEASE_KEY_PASSWORD")?.trim(),
        )
        return if (env.values.all { !it.isNullOrEmpty() }) env.mapValues { it.value!! } else null
    }

android {
    namespace = "cc.toolcrate.lyceum"
    compileSdk = 36

    defaultConfig {
        applicationId = "cc.toolcrate.lyceum"
        minSdk = 26
        targetSdk = 36
        // CI builds number themselves from the run (run 57 is version 1057), so every upload to
        // Play has a new, higher code. Builds made elsewhere use the fixed fallback.
        versionCode = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull()?.let { 1000 + it } ?: 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        // A fixed debug key, so every debug build has the same SHA-1 to register with Google for
        // the optional Drive backup (see docs/drive-setup.md). Debug only; never use it for a release.
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        // Your upload key, from keystore.properties (never committed) or, in CI, from environment
        // variables. Without either, release builds are unsigned. See docs/play-store/RELEASE.md.
        releaseSigning?.let { s ->
            create("release") {
                storeFile = file(s.getValue("storeFile"))
                storePassword = s.getValue("storePassword")
                keyAlias = s.getValue("keyAlias")
                keyPassword = s.getValue("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            signingConfigs.findByName("release")?.let { signingConfig = it }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // Exported Room schemas, so migration tests (Robolectric reads the app's merged assets) can build
    // each old version of the database. Debug builds only; release APKs don't carry them.
    sourceSets.getByName("debug").assets.srcDir("$projectDir/schemas")

    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.all {
            // Robolectric fetches Android's runtime jars itself; use the same Maven Central mirror.
            it.systemProperty("robolectric.dependency.repo.url", "https://maven-central.storage-download.googleapis.com/maven2/")
            it.systemProperty("robolectric.dependency.repo.id", "central-mirror")
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

// Play Store graphics are only drawn when asked for: ./gradlew testDebugUnitTest -Pstore=true
tasks.withType<Test>().configureEach {
    if (providers.gradleProperty("store").getOrElse("false") != "true") exclude("**/store/**")
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.generateKotlin", "true")
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.work.runtime)
    implementation(libs.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.biometric)
    implementation(libs.play.services.auth)
    debugImplementation(libs.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.room.testing)
    testImplementation(libs.work.testing)
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}
