pluginManagement {
    /*
     * The sample uses the Gradle plugin directly from this repository.
     *
     * A published consumer would remove includeBuild(...) and declare an
     * explicit plugin version in build.gradle.kts.
     */
    includeBuild("../../../openapi-generics-gradle-plugin")

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "openapi-generics-gradle-customer-service-client"
