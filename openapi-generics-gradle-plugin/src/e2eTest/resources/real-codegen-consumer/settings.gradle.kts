pluginManagement {
    val pluginRepository =
        providers.gradleProperty(
            "openApiGenericsPluginRepository"
        ).get()

    val openApiGeneratorVersion =
        providers.gradleProperty(
            "openApiGeneratorVersion"
        ).get()

    val openApiGenericsPluginVersion =
        providers.gradleProperty(
            "openApiGenericsPluginVersion"
        ).get()

    repositories {
        maven {
            url = uri(pluginRepository)

            content {
                includeGroup(
                    "io.github.blueprint-platform.openapi-generics"
                )
                includeGroup(
                    "io.github.blueprint-platform"
                )
            }
        }

        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        id("org.openapi.generator") version openApiGeneratorVersion
        id("io.github.blueprint-platform.openapi-generics") version openApiGenericsPluginVersion
    }
}

rootProject.name = "openapi-generics-real-codegen-consumer"