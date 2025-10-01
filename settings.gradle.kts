pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") } // 👉 ဒီလို ထည့်ပေးပါ
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // 👉 ဒီမှာလဲ ရှိပြီးသား
    }
}

rootProject.name = "POS"
include(":app")
