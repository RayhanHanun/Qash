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

        // --- TAMBAHKAN BARIS INI ---
        maven { url = java.net.URI("https://jitpack.io") }
        // ---------------------------
    }
}

rootProject.name = "Qash_FinalProject"
include(":app")