pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenLocal()
        maven("https://repo.auxilor.io/repository/maven-public/")
    }
}

rootProject.name = "EcoEnchants"

// Core
include(":eco-core")
include(":eco-core:core-plugin")
include(":eco-core:core-proxy")
include(":eco-core:core-nms")
include(":eco-core:core-nms:v1_20_R2")
