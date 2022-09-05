import com.google.protobuf.gradle.*

plugins {
    id("com.android.application")
    id("dagger.hilt.android.plugin")
    id("com.google.protobuf") version "0.8.17"
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
}

android {
    compileSdkVersion(31)
    defaultConfig {
        applicationId = "com.jkuester.unlauncher"
        minSdkVersion(21)
        targetSdkVersion(31)
        versionName = "2.0.0-beta.1"
        versionCode = 15
        vectorDrawables { useSupportLibrary = true }
//        signingConfigs {
//            if (project.extra.has("RELEASE_STORE_FILE")) {
//                register("release") {
//                    storeFile = file(project.extra["RELEASE_STORE_FILE"] as String)
//                    storePassword = project.extra["RELEASE_STORE_PASSWORD"] as String
//                    keyAlias = project.extra["RELEASE_KEY_ALIAS"] as String
//                    keyPassword = project.extra["RELEASE_KEY_PASSWORD"] as String
//                }
//            }
//        }
    }

    buildTypes {
        named("release").configure {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
//            signingConfig = signingConfigs.maybeCreate("release")
        }
        named("debug").configure {
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
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
    applicationVariants.all{
        outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName = "${applicationId}.apk"
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // Kotlin Libraries
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.5.21")

    // Support Libraries
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.0")
    implementation("androidx.datastore:datastore:1.0.0")
    implementation("androidx.datastore:datastore-core:1.0.0")
    implementation("com.google.protobuf:protobuf-javalite:3.10.0")

    // Arch Components
    implementation("androidx.core:core-ktx:1.7.0-beta01")
    implementation("androidx.fragment:fragment-ktx:1.3.6")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.3.1")
    implementation("androidx.room:room-runtime:2.2.5")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.2.0")
    kapt("androidx.room:room-compiler:2.2.5")

    //3rd party libs
    implementation("com.intuit.sdp:sdp-android:1.0.6")
    implementation("com.intuit.ssp:ssp-android:1.0.6")
    implementation("com.google.dagger:hilt-android:2.35")
    kapt("androidx.hilt:hilt-compiler:1.0.0")
    kapt("com.google.dagger:hilt-android-compiler:2.35")
}
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.17.3"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                id("java") {
                    option("lite")
                }
            }
        }
    }
}