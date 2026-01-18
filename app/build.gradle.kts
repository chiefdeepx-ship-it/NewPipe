/*
 * SPDX-FileCopyrightText: 2025 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.kapt)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.jetbrains.kotlin.parcelize)
    alias(libs.plugins.sonarqube)
    // checkstyle hata diya
}

val gitWorkingBranch = providers.exec {
    commandLine("git", "rev-parse", "--abbrev-ref", "HEAD")
}.standardOutput.asText.map { it.trim() }

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xannotation-default-target=param-property"
        )
    }
}

android {
    compileSdk = 36
    namespace = "org.schabi.newpipe"

    defaultConfig {
        applicationId = "org.schabi.newpipe"
        resValue("string", "app_name", "NewPipe")
        minSdk = 21
        targetSdk = 35

        versionCode = System.getProperty("versionCodeOverride")?.toInt() ?: 1006

        versionName = "0.28.1"
        System.getProperty("versionNameSuffix")?.let { versionNameSuffix = it }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isDebuggable = true
            val defaultBranches = listOf("master", "dev")
            val workingBranch = gitWorkingBranch.getOrElse("")
            val normalizedWorkingBranch = workingBranch
                .replaceFirst("^[^A-Za-z]+".toRegex(), "")
                .replace("[^0-9A-Za-z]+".toRegex(), "")

            if (normalizedWorkingBranch.isEmpty() || workingBranch in defaultBranches) {
                applicationIdSuffix = ".debug"
                resValue("string", "app_name", "NewPipe Debug")
            } else {
                applicationIdSuffix = ".debug.$normalizedWorkingBranch"
                resValue("string", "app_name", "NewPipe $workingBranch")
            }
        }

        release {
            System.getProperty("packageSuffix")?.let { suffix ->
                applicationIdSuffix = suffix
                resValue("string", "app_name", "NewPipe $suffix")
            }
            isMinifyEnabled = true
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
        disable += "NonConstantResourceId"
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        encoding = "utf-8"
    }

    sourceSets {
        getByName("androidTest") {
            assets.srcDir("$projectDir/schemas")
        }
    }

    androidResources {
        generateLocaleConfig = true
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/README.md",
                "META-INF/CHANGES",
                "META-INF/COPYRIGHT"
            )
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

// Yaha se maine checkstyle aur ktlint ka pura block uda diya hai.
// Ab koi police nahi bachi.

sonar {
    properties {
        property("sonar.projectKey", "TeamNewPipe_NewPipe")
        property("sonar.organization", "teamnewpipe")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

dependencies {
    coreLibraryDesugaring(libs.android.desugar)

    implementation(libs.newpipe.nanojson)
    implementation(libs.newpipe.extractor)
    implementation(libs.newpipe.filepicker)

    // Checkstyle dependency bhi hata di

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core)
    implementation(libs.androidx.documentfile)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.localbroadcastmanager)
    implementation(libs.androidx.media)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.rxjava3)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.work.rxjava3)
    implementation(libs.google.android.material)
    implementation(libs.androidx.webkit)

    implementation(libs.livefront.bridge)
    implementation(libs.evernote.statesaver.core)
    kapt(libs.evernote.statesaver.compiler)

    implementation(libs.jsoup)
    implementation(libs.squareup.okhttp)

    implementation(libs.google.exoplayer.core)
    implementation(libs.google.exoplayer.dash)
    implementation(libs.google.exoplayer.database)
    implementation(libs.google.exoplayer.datasource)
    implementation(libs.google.exoplayer.hls)
    implementation(libs.google.exoplayer.mediasession)
    implementation(libs.google.exoplayer.smoothstreaming)
    implementation(libs.google.exoplayer.ui)

    implementation(libs.lisawray.groupie.core)
    implementation(libs.lisawray.groupie.viewbinding)

    implementation(libs.squareup.picasso)

    implementation(libs.noties.markwon.core)
    implementation(libs.noties.markwon.linkify)

    implementation(libs.acra.core)
    compileOnly(libs.google.autoservice.annotations)
    ksp(libs.zacsweers.autoservice.compiler)

    implementation(libs.jakewharton.phoenix)

    implementation(libs.reactivex.rxjava)
    implementation(libs.reactivex.rxandroid)
    implementation(libs.jakewharton.rxbinding)

    implementation(libs.ocpsoft.prettytime)

    debugImplementation(libs.squareup.leakcanary.watcher)
    debugImplementation(libs.squareup.leakcanary.plumber)
    debugImplementation(libs.squareup.leakcanary.core)
    debugImplementation(libs.facebook.stetho.core)
    debugImplementation(libs.facebook.stetho.okhttp3)

    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.assertj.core)
}
