plugins {
    java
    id("com.gradleup.shadow") version("8.3.5")
    id("com.github.ben-manes.versions") version("0.51.0")
}

// Change to true when releasing
val release = false
val majorVersion = "1.14.1"
val minorVersion = if (release) "Release" else "DEV-" + System.getenv("BUILD_NUMBER")

group = "com.extendedclip"
version = "$majorVersion-$minorVersion"

repositories {
    mavenCentral()
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.glaremasters.me/repository/public/")
    maven("https://nexus.phoenixdevt.fr/repository/maven-public/")
    maven("https://repo.oraxen.com/releases")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly(libs.spigot)

    compileOnly(libs.vault)
    compileOnly(libs.authlib)

    compileOnly(libs.headdb)
    compileOnly(libs.itemsadder)
    compileOnly(libs.oraxen)
    compileOnly(libs.mythiclib)
    compileOnly(libs.mmoitems)
    compileOnly(libs.score)

    compileOnly(libs.papi)

    implementation(libs.nashorn)
    implementation(libs.adventure.platform)
    implementation(libs.adventure.minimessage)

    compileOnly("org.jetbrains:annotations:23.0.0")
}

tasks {
    shadowJar {
        relocate("org.objectweb.asm", "com.extendedclip.deluxemenus.libs.asm")
        relocate("org.openjdk.nashorn", "com.extendedclip.deluxemenus.libs.nashorn")
        relocate("net.kyori", "com.extendedclip.deluxemenus.libs.adventure")
        archiveFileName.set("DeluxeMenus-${rootProject.version}.jar")
    }
    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    processResources {
        filesMatching("plugin.yml") {
            expand("version" to rootProject.version)
        }
    }
}
