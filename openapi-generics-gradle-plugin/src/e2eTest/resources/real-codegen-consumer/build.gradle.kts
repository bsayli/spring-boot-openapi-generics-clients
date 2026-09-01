import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    java
    id("org.openapi.generator")
    id("io.github.blueprint-platform.openapi-generics")
}

val codegenRepository =
    providers.gradleProperty(
        "openApiGenericsCodegenRepository"
    ).get()

val openApiGenericsVersion =
    providers.gradleProperty(
        "openApiGenericsPluginVersion"
    ).get()

repositories {
    maven {
        url = uri(codegenRepository)

        content {
            includeGroup(
                "io.github.blueprint-platform"
            )
        }
    }

    mavenCentral()
}

dependencies {
    /*
     * Generated wrappers extend the platform-owned generic contracts.
     * This artifact is built into the isolated repository by
     * prepareRealCodegenRepository.
     */
    implementation(
        "io.github.blueprint-platform:openapi-generics-contract:$openApiGenericsVersion"
    )

    compileOnly(
        "com.fasterxml.jackson.core:jackson-annotations:2.18.2"
    )
    compileOnly(
        "jakarta.annotation:jakarta.annotation-api:3.0.0"
    )
    compileOnly(
        "jakarta.validation:jakarta.validation-api:3.1.1"
    )
}

tasks.named<GenerateTask>("openApiGenerate") {
    generatorName.set(
        "java-generics-contract"
    )

    inputSpec.set(
        layout.projectDirectory
            .file("src/main/openapi/service-response-e2e.yaml")
            .asFile
            .absolutePath
    )

    outputDir.set(
        layout.buildDirectory
            .dir("generated/openapi")
            .get()
            .asFile
            .absolutePath
    )

    /*
     * Match the maintained Maven client samples. The custom generator
     * specializes the upstream Java RestClient/Jackson templates.
     */
    library.set("restclient")

    apiPackage.set(
        "io.github.blueprintplatform.functional.generated.api"
    )
    modelPackage.set(
        "io.github.blueprintplatform.functional.generated.model"
    )
    invokerPackage.set(
        "io.github.blueprintplatform.functional.generated.invoker"
    )

    configOptions.set(
        mapOf(
            "sourceFolder" to "src/gen/java",
            "useSpringBoot3" to "true",
            "serializationLibrary" to "jackson",
            "openApiNullable" to "false"
        )
    )

    /*
     * This E2E is deliberately model-focused. It verifies contract-aware
     * reconstruction and Java compilation without pulling HTTP transport
     * generation into the same test.
     */
    globalProperties.set(
        mapOf(
            "models" to "",
            "apis" to "false",
            "supportingFiles" to "false",
            "modelDocs" to "false",
            "modelTests" to "false",
            "apiDocs" to "false",
            "apiTests" to "false"
        )
    )

    cleanupOutput.set(true)
    skipValidateSpec.set(false)
}