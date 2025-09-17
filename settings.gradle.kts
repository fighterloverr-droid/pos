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
        // Chart Library အတွက် ဒီနေရာမှာ ထည့်ပါ
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "POS"
include(":app")