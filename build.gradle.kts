plugins {
    java
    `java-library`
    `maven-publish`
    kotlin("jvm") version "1.9.22"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.willfp.libreforge-gradle-plugin") version "1.0.0"
}

group = "com.willfp"
version = findProperty("version")!!
val libreforgeVersion = findProperty("libreforge-version")

base {
    archivesName.set(project.name)
}

dependencies {
    implementation(project(":eco-core:core-plugin"))
    implementation(project(":eco-core:core-nms:v1_20_R2"))
    implementation(project(":eco-core:core-nms:v1_20_R3", configuration = "reobf"))
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")
    apply(plugin = "com.github.johnrengelman.shadow")

    repositories {
        mavenLocal()
        mavenCentral()

        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.auxilor.io/repository/maven-public/")
        maven("https://jitpack.io")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://repo.codemc.org/repository/nms/")
        maven("https://repo.essentialsx.net/releases/")
    }

    dependencies {
        compileOnly("com.willfp:eco:6.68.5")
        compileOnly("org.jetbrains:annotations:24.0.1")
        compileOnly("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
        compileOnly("com.github.ben-manes.caffeine:caffeine:3.1.8")
    }

    java {
        withSourcesJar()
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    }

    tasks {
        shadowJar {
            relocate("com.willfp.libreforge.loader", "com.willfp.ecoenchants.libreforge.loader")
        }

        compileKotlin {
            kotlinOptions {
                jvmTarget = "17"
            }
        }

        compileJava {
            options.isDeprecation = true
            options.encoding = "UTF-8"

            dependsOn(clean)
        }

        processResources {
            filesMatching(listOf("**plugin.yml", "**eco.yml")) {
                expand(
                    "version" to project.version,
                    "libreforgeVersion" to libreforgeVersion,
                    "pluginName" to rootProject.name
                )
            }
        }

        build {
            dependsOn(shadowJar)
        }
    }
}
