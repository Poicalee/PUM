pluginManagement {
    repositories {
        google()  // Add this line if not already present
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()  // Add this line if not already present
        mavenCentral()
    }
}

rootProject.name = "YourProjectName"
include(":app")