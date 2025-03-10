import com.google.protobuf.gradle.id

plugins {
    id("com.android.application")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
    id("com.google.protobuf")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jetbrains.kotlinx.kover")
    kotlin("android")
}

android {
    compileSdk = 35
    defaultConfig {
        applicationId = "com.jkuester.unlauncher"
        minSdk = 21
        targetSdk = 35
        versionName = "2.2.0-beta.1"
        versionCode = 22
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
    buildFeatures {
        viewBinding = true
        buildConfig = true
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
            enableUnitTestCoverage = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
    lint {
        warningsAsErrors = true
        disable += "Typos" // Too many false positives
        disable += "VectorPath" // Not planning to change "large" graphics for now
        disable += "GradleDependency" // Do not fail linting due to new dependencies
        checkDependencies = false
    }
    namespace = "com.sduduzog.slimlauncher"
    applicationVariants.all {
        outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "$applicationId.apk"
        }
        assembleProvider.get().dependsOn.add("ktlintCheck")
    }
}

dependencies {
    // Kotlin Libraries
    // This needs to match ksp and kotlin-gradle-plugin
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.0")

    // Support Libraries
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.datastore:datastore:1.1.1")
    implementation("androidx.datastore:datastore-core:1.1.1")
    implementation("com.google.protobuf:protobuf-javalite:4.29.2")

    // Arch Components
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.5")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // 3rd party libs
    implementation("com.intuit.sdp:sdp-android:1.1.1")
    implementation("com.intuit.ssp:ssp-android:1.1.1")
    implementation("com.google.dagger:hilt-android:2.54")
    ksp("androidx.hilt:hilt-compiler:1.2.0")
    ksp("com.google.dagger:hilt-android-compiler:2.54")

    // Test libs
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("io.mockk:mockk-android:1.13.14")
    testImplementation("io.mockk:mockk-agent:1.13.14")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
}
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.29.2"
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
ktlint {
    android = true
    ignoreFailures = false
}
kover {
    reports {
        filters {
            excludes {
                packages(
                    "com.sduduzog.slimlauncher",
                    "com.jkuester.unlauncher.datastore.proto",
                    "dagger.hilt.internal.aggregatedroot.codegen",
                    "hilt_aggregated_deps",
                )
                annotatedBy(
                    "javax.annotation.processing.Generated",
                    "dagger.internal.DaggerGenerated",
                )
                classes(
                    "*Hilt_*",
                )
            }
        }
        verify {
            rule {
                minBound(100)
            }
        }
    }
}
