import com.cognifide.gradle.aem.pkg.ComposeTask

plugins {
    id("com.cognifide.aem.bundle")
    kotlin("jvm")
}

description = "Example - Common"

tasks.named<ComposeTask>("aemCompose") {
    fromJar("org.jetbrains.kotlin:kotlin-osgi-bundle:1.2.21")
}

dependencies {
    compileOnly("org.hashids:hashids:1.0.1")
}

aem {
    bundle {
        javaPackage = "com.company.aem.example.common"
        exportPackage("org.hashids")
    }
}