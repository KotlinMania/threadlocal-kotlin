import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    kotlin("multiplatform") version "2.3.20"
    kotlin("plugin.serialization") version "2.3.20"
    id("org.jetbrains.kotlinx.atomicfu") version "0.27.0"
    id("com.android.kotlin.multiplatform.library") version "9.2.0"
    id("com.vanniktech.maven.publish") version "0.30.0"
}

group = "io.github.kotlinmania"
version = "0.1.1"

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

    val xcf = XCFramework("TlsKotlin")

    macosArm64 {
        binaries.framework {
            baseName = "TlsKotlin"
            xcf.add(this)
        }
    }
    macosX64 {
        binaries.framework {
            baseName = "TlsKotlin"
            xcf.add(this)
        }
    }
    linuxX64()
    mingwX64()
    iosArm64 {
        binaries.framework {
            baseName = "TlsKotlin"
            xcf.add(this)
        }
    }
    iosX64 {
        binaries.framework {
            baseName = "TlsKotlin"
            xcf.add(this)
        }
    }
    iosSimulatorArm64 {
        binaries.framework {
            baseName = "TlsKotlin"
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

    androidLibrary {
        namespace = "io.github.kotlinmania.tls"
        compileSdk = 34
        minSdk = 24
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
                implementation("org.jetbrains.kotlinx:atomicfu:0.27.0")
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

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    val signingConfigured =
        providers.gradleProperty("signingInMemoryKey").isPresent ||
            providers.gradleProperty("signing.keyId").isPresent ||
            providers.environmentVariable("SIGNING_KEY").isPresent
    if (signingConfigured) {
        signAllPublications()
    }

    coordinates(group.toString(), "tls-kotlin", version.toString())

    pom {
        name.set("tls-kotlin")
        description.set(
            "Kotlin Multiplatform port of Rust's thread_local crate, " +
                "providing TLS and execution-local storage for Kotlin Native first."
        )
        inceptionYear.set("2026")
        url.set("https://github.com/KotlinMania/tls-kotlin")

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
            url.set("https://github.com/KotlinMania/tls-kotlin")
            connection.set("scm:git:git://github.com/KotlinMania/tls-kotlin.git")
            developerConnection.set("scm:git:ssh://github.com/KotlinMania/tls-kotlin.git")
        }
    }
}
