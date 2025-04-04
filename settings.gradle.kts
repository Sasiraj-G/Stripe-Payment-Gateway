pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }


        mavenCentral()
        gradlePluginPortal()
        maven { setUrl("https://jitpack.io") }



    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://cdn.veriff.me/android/") }
        google()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }




    }
}

rootProject.name = "PaymentGateway"
include(":app")
 