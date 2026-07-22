pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "GameLauncher"
include(":app")

// Core modules (Step 1, 2A, 2B, 2C, 2D & 2E active)
include(":core:shizuku")
include(":core:settings")
include(":core:device")
include(":core:permissions")
include(":core:database")
include(":core:di")

// Feature modules
include(":feature:tweaks")
include(":feature:network")
include(":feature:monitor")
// include(":feature:performance")
// include(":feature:game-list")
