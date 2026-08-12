plugins {
    java
    id("com.gradleup.shadow") version("9.6.1")
    id("io.github.ben-manes.versions") version("0.61.0")
}

// Change to true when releasing
val release = false
val majorVersion = "1.14.2"
val minorVersion = if (release) "Release" else "DEV-" + System.getenv("BUILD_NUMBER")

group = "com.extendedclip"
version = "$majorVersion-$minorVersion"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://nexus.phoenixdevt.fr/repository/maven-public/")
    maven("https://repo.momirealms.net/releases/")
    maven("https://repo.nexomc.com/releases/")
    maven("https://repo.oraxen.com/releases")
    maven("https://maven.devs.beer/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly(libs.paper)

    compileOnly(libs.vault)
    compileOnly(libs.authlib)

    compileOnly(libs.headdb)
    compileOnly(libs.headdb.api)
    compileOnly(libs.craftengine.core)
    compileOnly(libs.craftengine.bukkit)
    compileOnly(libs.itemsadder)
    compileOnly(libs.nexo)
    compileOnly(libs.oraxen)
    compileOnly(libs.mythiclib)
    compileOnly(libs.mmoitems)
    compileOnly(libs.score)
    compileOnly(libs.sig)

    compileOnly(libs.papi)

    implementation(libs.nashorn)
    implementation(libs.bstats)

    compileOnly("org.jetbrains:annotations:26.1.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    disableAutoTargetJvm()
}

tasks {
    withType<JavaCompile> {
        options.release.set(21)
    }

    shadowJar {
        relocate("org.objectweb.asm", "com.extendedclip.deluxemenus.libs.asm")
        relocate("org.openjdk.nashorn", "com.extendedclip.deluxemenus.libs.nashorn")
        relocate("org.bstats", "com.extendedclip.deluxemenus.libs.bstats")
        archiveFileName.set("DeluxeMenus-${rootProject.version}.jar")
    }

    processResources {
        filesMatching("paper-plugin.yml") {
            expand("version" to rootProject.version)
        }
    }
}
