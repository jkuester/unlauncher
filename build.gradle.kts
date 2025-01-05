// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.7.3")
        // This needs to match kotlin-stdlib
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.54")

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
    id("com.google.devtools.ksp") version "2.1.0-1.0.29" apply false // This needs to match kotlin-stdlib
    id("com.google.protobuf") version "0.9.4" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
}
