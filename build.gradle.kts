import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "1.9.24" 
    id("org.jetbrains.compose") version "1.6.10"
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(21))
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {

    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)


    implementation("media.kamel:kamel-image:0.9.3")
    implementation("io.ktor:ktor-client-cio:2.3.8")

    implementation("org.postgresql:postgresql:42.7.3") 

    implementation("org.jetbrains.exposed:exposed-core:0.49.0") 
    implementation("org.jetbrains.exposed:exposed-dao:0.49.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.49.0")

    implementation("org.mindrot:jbcrypt:0.4")
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "CarMaintenanceApp"
            packageVersion = "1.0.0"

            description = "Aplicación de Gestión de Mantenimiento de Coches"
            copyright = "© 2024 Car Maintenance App. All rights reserved."
            vendor = "Car Maintenance Solutions"

            windows {
                menuGroup = "Car Maintenance"
                upgradeUuid = "18159995-d967-4CD2-8885-77BFA97CFA9F"
            }

            macOS {
                bundleID = "com.example.carmaintenance"
            }

            linux {
                packageName = "car-maintenance-app"
                debMaintainer = "maintenance@example.com"
                menuGroup = "Office"
                appRelease = "1"
                appCategory = "Office"
            }
        }
    }
}

tasks.register<JavaExec>("runTestMain") {
    group = "application"
    description = "Run the lightweight test main (test.TestMainKt)"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("test.TestMainKt")
    javaLauncher.set(javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(21)) })
}