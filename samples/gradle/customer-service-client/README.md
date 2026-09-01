# Gradle Customer Service Client

> A user-facing Kotlin DSL sample for generating and compiling an OpenAPI Generics Java client with the Gradle plugin.

This sample is the Gradle counterpart of the Spring Boot 3 Maven-generated customer client. It
reuses the same committed OpenAPI document and shared `CustomerDto` contract, but performs client
generation through:

```text
org.openapi.generator
        +
io.github.blueprint-platform.openapi-generics
```

The sample is intentionally a **client-generation module**, not another producer or consumer
application.

## Table of Contents

- [What This Sample Demonstrates](#what-this-sample-demonstrates)
- [Project Layout](#project-layout)
- [Generation Flow](#generation-flow)
- [Local Repository Prerequisite](#local-repository-prerequisite)
- [Run the Sample](#run-the-sample)
- [Expected Output](#expected-output)
- [What the Consumer Configures](#what-the-consumer-configures)
- [What the Plugin Configures Automatically](#what-the-plugin-configures-automatically)
- [Published Plugin Usage](#published-plugin-usage)
- [Design Boundary](#design-boundary)

## What This Sample Demonstrates

The sample verifies a realistic single-spec Gradle adoption path:

- the official OpenAPI Generator Gradle plugin remains the generation engine;
- `java-generics-contract` is selected on a normal `GenerateTask`;
- the OpenAPI Generics Gradle plugin prepares and applies the effective templates;
- the custom generator runtime is supplied to the worker classpath;
- generated sources are registered on the `main` Java source set;
- `compileJava` depends on the selected generation task;
- BYOC maps the projected customer schema back to the shared `CustomerDto`;
- direct and paged generic wrappers compile as part of the normal Gradle build;
- configuration cache can be reused on subsequent builds.

## Project Layout

```text
samples/gradle/customer-service-client
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── README.md
└── src/main/java/
    └── io/github/blueprintplatform/samples/gradle/customer/
        └── GeneratedCustomerClientProbe.java
```

The authoritative OpenAPI input remains here:

```text
samples/spring-boot-3/spec/customer-api-docs.yaml
```

The sample does not copy or fork that specification.

## Generation Flow

```text
customer-api-docs.yaml
        ↓
generateCustomerClient
        ↓
java-generics-contract
        ↓
build/generated/customer/src/gen/java
        ↓
main Java source set
        ↓
compileJava
```

Representative generated contracts:

```java
ServiceResponse<CustomerDto>

ServiceResponse<Page<CustomerDto>>
```

The generated wrapper classes bind generic parameters. They do not become owners of the shared
envelope or customer contract.

## Local Repository Prerequisite

The sample consumes snapshot artifacts from Maven local while using the Gradle plugin source
directly through `includeBuild`.

From the repository root, install the codegen, contract, and customer contract artifacts:

```bash
./mvnw   -pl openapi-generics-java-codegen,samples/domain-contracts/customer-contract   -am   install   -DskipTests
```

When no Maven wrapper is present, use the same command with `mvn`.

This prerequisite is specific to running the unreleased `1.3.0-SNAPSHOT` sample directly from the
repository. Published consumers resolve released artifacts from their configured Maven repository.

## Run the Sample

From the repository root:

```bash
./openapi-generics-gradle-plugin/gradlew   -p samples/gradle/customer-service-client   clean build
```

Run it a second time to observe configuration-cache reuse:

```bash
./openapi-generics-gradle-plugin/gradlew   -p samples/gradle/customer-service-client   build
```

The sample deliberately reuses the repository's Gradle wrapper instead of maintaining a duplicate
wrapper distribution.

## Expected Output

Generated sources:

```text
samples/gradle/customer-service-client/
└── build/
    └── generated/
        └── customer/
            └── src/gen/java/
                └── io/github/blueprintplatform/samples/gradle/customer/generated/
                    ├── api/
                    ├── dto/
                    └── invoker/
```

Compiled proof class:

```text
build/classes/java/main/
└── io/github/blueprintplatform/samples/gradle/customer/
    └── GeneratedCustomerClientProbe.class
```

`GeneratedCustomerClientProbe` imports API and wrapper classes from the generated tree. The build
therefore fails if generation does not run, generated sources are not registered, or generic
reconstruction produces unexpected packages or class names.

## What the Consumer Configures

The sample owns normal OpenAPI Generator choices:

- `generatorName`;
- `inputSpec`;
- `outputDir`;
- client library;
- API, model, and invoker packages;
- OpenAPI Generator `configOptions`;
- BYOC mappings;
- validation and documentation/test generation switches.

This keeps the official `GenerateTask` model visible and familiar.

## What the Plugin Configures Automatically

The OpenAPI Generics Gradle plugin supplies the integration lifecycle:

- custom codegen runtime registration;
- selected-task generator worker classpath;
- upstream template resolution;
- effective Java template preparation and patching;
- selected-task `templateDir`;
- generated Java source registration;
- `compileJava` dependency wiring;
- compatibility checks against the supported OpenAPI Generator plugin;
- configuration-cache-compatible task wiring.

The consumer intentionally does not configure:

```kotlin
generatorClasspath.from(...)
templateDir.set(...)
sourceSets.main...
compileJava.dependsOn(...)
```

Those are plugin-owned responsibilities.

## Published Plugin Usage

Inside this repository, `settings.gradle.kts` uses:

```kotlin
includeBuild("../../../openapi-generics-gradle-plugin")
```

A published consumer removes that composite-build entry and declares the released plugin version:

```kotlin
plugins {
    java
    id("org.openapi.generator") version "7.25.0"
    id("io.github.blueprint-platform.openapi-generics") version "<released-version>"
}
```

The remaining `GenerateTask` configuration stays the same.

## Design Boundary

This sample validates Gradle adoption and compilation.

It intentionally does not add:

- another producer service;
- another consumer service;
- runtime HTTP calls;
- transport regression coverage;
- multiple generation tasks.

Those concerns are already covered by the Spring Boot integration stacks, transport coverage, and
the Gradle plugin's functional and real-codegen E2E tests.

The sample's single responsibility is to provide a clear reference for a real Gradle
client-generation module.
