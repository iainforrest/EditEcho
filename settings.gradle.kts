// settings.gradle.kts
pluginManagement {
    repositories {
        gradlePluginPortal()     // must be first for version-catalog-aware plugins
        google()
        mavenCentral()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "EditEcho"
include(":app")
