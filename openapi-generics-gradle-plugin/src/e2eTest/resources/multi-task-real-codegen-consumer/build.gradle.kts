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

fun GenerateTask.configureModelOnlyGeneration() {
    generatorName.set(
        "java-generics-contract"
    )

    library.set("restclient")

    configOptions.set(
        mapOf(
            "sourceFolder" to "src/gen/java",
            "useSpringBoot3" to "true",
            "serializationLibrary" to "jackson",
            "openApiNullable" to "false"
        )
    )

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

tasks.register<GenerateTask>("generateCustomerClient") {
    configureModelOnlyGeneration()

    inputSpec.set(
        layout.projectDirectory
            .file("src/main/openapi/customer.yaml")
            .asFile
            .absolutePath
    )

    outputDir.set(
        layout.buildDirectory
            .dir("generated/customer")
            .get()
            .asFile
            .absolutePath
    )

    modelPackage.set(
        "io.github.blueprintplatform.functional.multitask.customer.model"
    )
}

tasks.register<GenerateTask>("generateOrderClient") {
    configureModelOnlyGeneration()

    inputSpec.set(
        layout.projectDirectory
            .file("src/main/openapi/order.yaml")
            .asFile
            .absolutePath
    )

    outputDir.set(
        layout.buildDirectory
            .dir("generated/order")
            .get()
            .asFile
            .absolutePath
    )

    modelPackage.set(
        "io.github.blueprintplatform.functional.multitask.order.model"
    )
}
