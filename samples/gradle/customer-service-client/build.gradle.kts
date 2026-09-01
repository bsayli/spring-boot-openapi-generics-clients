import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    java
    id("org.openapi.generator") version "7.25.0"
    id("io.github.blueprint-platform.openapi-generics")
}

group = "io.github.blueprint-platform.samples"
version = "1.3.0-SNAPSHOT"

val openApiGenericsVersion = "1.3.0-SNAPSHOT"
val springBootVersion = "3.5.16"

repositories {
    /*
     * Snapshot platform and sample contract artifacts are installed by the
     * repository-level Maven prerequisite documented in README.md.
     */
    mavenLocal()
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation(
        "io.github.blueprint-platform:openapi-generics-contract:$openApiGenericsVersion"
    )

    implementation(
        "io.github.blueprint-platform.samples:customer-contract:$openApiGenericsVersion"
    )

    implementation(
        platform(
            "org.springframework.boot:spring-boot-dependencies:$springBootVersion"
        )
    )

    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.springframework.boot:spring-boot-starter")
    compileOnly("jakarta.validation:jakarta.validation-api")
    compileOnly("jakarta.annotation:jakarta.annotation-api")
    compileOnly("org.apache.httpcomponents.client5:httpclient5")
    compileOnly("org.slf4j:slf4j-api")
}

tasks.register<GenerateTask>("generateCustomerClient") {
    generatorName.set("java-generics-contract")

    inputSpec.set(
        layout.projectDirectory
            .file("../../spring-boot-3/spec/customer-api-docs.yaml")
            .asFile
            .absolutePath
    )

    outputDir.set(
        layout.buildDirectory.dir("generated/customer")
    )

    library.set("restclient")

    apiPackage.set(
        "io.github.blueprintplatform.samples.gradle.customer.generated.api"
    )

    modelPackage.set(
        "io.github.blueprintplatform.samples.gradle.customer.generated.dto"
    )

    invokerPackage.set(
        "io.github.blueprintplatform.samples.gradle.customer.generated.invoker"
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
     * BYOC mapping: generated wrappers bind the shared CustomerDto contract
     * rather than generating and owning a parallel customer model.
     */
    additionalProperties.set(
        mapOf(
            "openapi-generics.response-contract.CustomerDto" to
                "io.github.blueprintplatform.contracts.customer.CustomerDto"
        )
    )

    cleanupOutput.set(true)
    skipValidateSpec.set(false)

    generateApiDocumentation.set(false)
    generateApiTests.set(false)
    generateModelDocumentation.set(false)
    generateModelTests.set(false)
}
