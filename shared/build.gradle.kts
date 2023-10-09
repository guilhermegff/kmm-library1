plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("maven-publish")
}

val libName = "KmmLibrary1"
val libVersion = "1.0.0"
group = "com.project.kmm_library1"

version = libVersion

publishing {
    publications.withType<MavenPublication> {
        artifactId = libName
    }
}

kotlin {
    targetHierarchy.default()

    android {
        publishLibraryVariants("release", "debug")
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()
}

android {
    namespace = "com.project.kmm_library1"
    compileSdk = 33
    defaultConfig {
        minSdk = 24
    }
}