// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22") // This needs to match kotlin-stdlib
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.50")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

plugins {
    id("com.google.devtools.ksp") version "1.9.22-1.0.16" apply false // This needs to match kotlin-stdlib
    id("com.google.protobuf") version "0.9.4" apply false
}
