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
    /*repositories {
        maven {
            url = uri(System.getenv("MAVEN_WRITE_URL"))
            credentials {
                password = System.getenv("MAVEN_PWD")
                username = System.getenv("MAVEN_USERNAME")
            }
        }
    }*/
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

    cocoapods {
        name = "KmmLibrary1Pod"
        summary = "KmmLibrary1 Shared Module"
        homepage = "Link to the Shared Module homepage"
        ios.deploymentTarget = "14.1"
        version = libVersion
        source =
            "{ :git => 'https://github.com/guilhermegff/measure-converter.git', :tag => '$libVersion' }"
        publishDir = rootProject.file("pods")
        license = "{ :type => 'MIT', :text => 'License Text' }"
        framework {
            baseName = "KmmLibrary1"
        }

        //Maps custom XCode configuration to NativeBuildType
        xcodeConfigurationToNativeBuildType["CUSTOM_DEBUG"] =
            org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType.DEBUG
        xcodeConfigurationToNativeBuildType["CUSTOM_RELEASE"] =
            org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType.RELEASE
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                //Put multiplatform dependencies here
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

android {
    namespace = "com.project.kmm_library1"
    compileSdk = 33
    defaultConfig {
        minSdk = 24
    }
}

task("checkoutDev", type = Exec::class) {
    workingDir = File("$rootDir/pods")
    commandLine("git", "checkout", "develop").standardOutput
}

task("checkoutMaster", type = Exec::class) {
    workingDir = File("$rootDir/pods")
    commandLine("git", "checkout", "master").standardOutput
}

task("publishDevXCFramework") {
    description = "Publish iOS framework to the Cocoa Pods Repository"
    dependsOn("checkoutDev", "podPublishDebugFramework")

    doLast {
        val dir = File("$rootDir/pods/debug/${libName}Pod.podspec")
        val tempFile = File("$rootDir/pods/debug/${libName}Pod.podspec.new")

        val reader = dir.bufferedReader()
        val writer = tempFile.bufferedWriter()
        var currentLine: String?

        while (reader.readLine().also { line -> currentLine = line } != null) {
            if (currentLine?.startsWith("s.version") == true) {
                writer.write("s..version = \"${libVersion}\"" + System.lineSeparator())
            } else {
                writer.write(currentLine + System.lineSeparator())
            }
            if (currentLine?.startsWith("s.vendored_frameworks") == true) {
                writer.write("s.vendored_frameworks = \"$rootDir/pods/debug/KmmLibrary1.xcframework\"" + System.lineSeparator())
            } else {
                writer.write(currentLine + System.lineSeparator())
            }
        }
        writer.close()
        reader.close()
        val successful = tempFile.renameTo(dir)

        copy {
            from(tempFile.path)
            into(rootDir.path)
        }

        if (successful) {
            project.exec {
                workingDir = File("$rootDir/pods")
                commandLine(
                    "git", "commit", "-a", "-m", "\"New dev release: ${libVersion}}\""
                ).standardOutput
            }

            project.exec {
                workingDir = File("$rootDir/pods")
                commandLine("git", "tag", libVersion).standardOutput
            }

            project.exec {
                workingDir = File("$rootDir/pods")
                commandLine("git", "push", "origin", "develop", "--tags").standardOutput
            }
        }
    }
}

task("publishReleaseXCFramework") {
    description = "Publish iOS Framework to the Cocoa Pod Repository"
    dependsOn("checkoutMaster", "podPublishReleaseXCFramework")

    doLast {
        val dir = File("$rootDir/pods/release/${libName}Pod.podspec")
        val tempFile = File("$rootDir/pods/release/${libName}Pod.podspec.new")

        val reader = dir.bufferedReader()
        val writer = tempFile.bufferedWriter()
        var currentLine: String?

        while (reader.readLine().also { line -> currentLine = line } != null) {
            if (currentLine?.startsWith("s.version") == true) {
                writer.write("s.version = \"${libVersion}\"" + System.lineSeparator())
            } else {
                writer.write(currentLine + System.lineSeparator())
            }
        }
        writer.close()
        reader.close()
        val successful = tempFile.renameTo(dir)

        if (successful) {
            project.exec {
                workingDir = File("$rootDir/pods")
                commandLine(
                    "git", "commit", "-a", "-m", "\"New release: ${libVersion}}\""
                ).standardOutput
            }

            project.exec {
                workingDir = File("$rootDir/pods")
                commandLine("git", "tag", libVersion).standardOutput
            }

            project.exec {
                workingDir = File("$rootDir/pods")
                commandLine("git", "push", "origin", "master", "--tags").standardOutput
            }
        }

    }
}