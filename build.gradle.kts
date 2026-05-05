import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
    kotlin("multiplatform") version "2.3.21"
    kotlin("plugin.serialization") version "2.3.21"
    id("com.android.kotlin.multiplatform.library") version "9.2.0"
    id("com.vanniktech.maven.publish") version "0.36.0"
}

group = "io.github.kotlinmania"
version = "0.2.1"

val androidSdkDir: String? =
    providers.environmentVariable("ANDROID_SDK_ROOT").orNull
        ?: providers.environmentVariable("ANDROID_HOME").orNull
        ?: rootProject.file(".android-sdk").takeIf { it.exists() }?.absolutePath

if (androidSdkDir != null && file(androidSdkDir).exists()) {
    val localProperties = rootProject.file("local.properties")
    if (!localProperties.exists()) {
        val sdkDirPropertyValue = file(androidSdkDir).absolutePath.replace("\\", "/")
        localProperties.writeText("sdk.dir=$sdkDirPropertyValue")
    }
}

kotlin {
    applyDefaultHierarchyTemplate()

    compilerOptions {
        allWarningsAsErrors.set(true)
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    val xcf = XCFramework("ThreadLocalKotlin")

    macosArm64 {
        binaries.framework {
            baseName = "ThreadLocalKotlin"
            xcf.add(this)
        }
    }
    linuxX64()
    mingwX64()
    iosArm64 {
        binaries.framework {
            baseName = "ThreadLocalKotlin"
            xcf.add(this)
        }
    }
    iosSimulatorArm64 {
        binaries.framework {
            baseName = "ThreadLocalKotlin"
            xcf.add(this)
        }
    }
    js {
        browser()
        nodejs()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }

    swiftExport {
        moduleName = "ThreadLocalKotlin"
        flattenPackage = "io.github.kotlinmania.threadlocal"
    }

    android {
        namespace = "io.github.kotlinmania.threadlocal"
        compileSdk = 34
        minSdk = 24
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
                implementation("org.jetbrains.kotlinx:atomicfu:0.32.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
            }
        }
    }
    jvmToolchain(21)
}

rootProject.extensions.configure<YarnRootExtension>("kotlinYarn") {
    resolution("diff", "8.0.3")
    resolution("**/diff", "8.0.3")
    resolution("serialize-javascript", "7.0.5")
    resolution("**/serialize-javascript", "7.0.5")
    resolution("webpack", "5.106.2")
    resolution("**/webpack", "5.106.2")
    resolution("follow-redirects", "1.16.0")
    resolution("**/follow-redirects", "1.16.0")
    resolution("lodash", "4.18.1")
    resolution("**/lodash", "4.18.1")
    resolution("ajv", "8.20.0")
    resolution("**/ajv", "8.20.0")
    resolution("brace-expansion", "5.0.5")
    resolution("**/brace-expansion", "5.0.5")
    resolution("flatted", "3.4.2")
    resolution("**/flatted", "3.4.2")
    resolution("minimatch", "10.2.5")
    resolution("**/minimatch", "10.2.5")
    resolution("picomatch", "4.0.4")
    resolution("**/picomatch", "4.0.4")
    resolution("qs", "6.15.1")
    resolution("**/qs", "6.15.1")
    resolution("socket.io-parser", "4.2.6")
    resolution("**/socket.io-parser", "4.2.6")
}

mavenPublishing {
    publishToMavenCentral()
    val signingConfigured =
        providers.gradleProperty("signingInMemoryKey").isPresent ||
            providers.gradleProperty("signing.keyId").isPresent ||
            providers.environmentVariable("SIGNING_KEY").isPresent
    if (signingConfigured) {
        signAllPublications()
    }

    coordinates(group.toString(), "threadlocal-kotlin", version.toString())

    pom {
        name.set("threadlocal-kotlin")
        description.set(
            "Kotlin Multiplatform port of the Rust thread_local crate " +
                "(Amanieu/thread_local-rs v1.1.9): per-object thread-local " +
                "storage with O(1) lock-free reads, CAS-allocated buckets, " +
                "and cross-thread iter()/iterMut()/intoIter()."
        )
        inceptionYear.set("2026")
        url.set("https://github.com/KotlinMania/threadlocal-kotlin")

        licenses {
            license {
                name.set("Apache-2.0")
                url.set("https://opensource.org/licenses/Apache-2.0")
                distribution.set("repo")
            }
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("sydneyrenee")
                name.set("Sydney Renee")
                email.set("sydney@solace.ofharmony.ai")
                url.set("https://github.com/sydneyrenee")
            }
        }

        scm {
            url.set("https://github.com/KotlinMania/threadlocal-kotlin")
            connection.set("scm:git:git://github.com/KotlinMania/threadlocal-kotlin.git")
            developerConnection.set("scm:git:ssh://github.com/KotlinMania/threadlocal-kotlin.git")
        }
    }
}
