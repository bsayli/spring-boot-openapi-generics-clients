---
layout: default
title: Client-Side Build Setup
parent: Client-Side Adoption
nav_order: 1
---

# Client-Side Build Setup — Maven Plugins & Dependencies

This guide describes how to configure a **client module build** to generate
**type-safe OpenAPI clients** with **deterministic wrapper typing** and
**without duplicating shared response contracts**.

### Contract usage

Client generation assumes the following conventions:

* Successful responses use the shared contract provided by  
  **`io.github.bsayli:api-contract`**, specifically **`ServiceResponse<T>`**.
* Nested generics are supported for **`ServiceResponse<Page<T>>`** only.
  Other generic nesting patterns are treated using the generator’s default behavior.

These conventions allow the client build to remain predictable while avoiding
endpoint-specific wrapper models.

---

The build pipeline presented in this document is intentionally explicit and reproducible.
At a high level, it:

1. extracts the upstream OpenAPI Generator templates as a stable baseline,
2. overlays project-specific Mustache customizations,
3. generates client sources using the effective template set,
4. compiles the generated sources as part of the normal Maven build.

With this setup, client generation stays **deterministic**, **template-safe**,
and aligned with the published OpenAPI specification — without introducing
parallel or duplicated response models.

---

## 📑 Table of Contents

- [1) Maven Properties](#1-maven-properties)
- [2) Core Dependencies](#2-core-dependencies)
- [3) Maven Plugins — Full Build Pipeline](#3-maven-plugins--full-build-pipeline)
- [4) Why These Plugins Exist (and Why This Order Matters)](#4-why-these-plugins-exist-and-why-this-order-matters)
- [5) Mustache Contract Integration (What Your Templates Must Do)](#5-mustache-contract-integration-template-responsibilities)
- [6) Verification](#6-verification)

---

## 1) Maven Properties

Pin versions for deterministic generation and reproducible builds.

```xml
<properties>
  <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
  <java.version>21</java.version>

  <spring-boot.version>3.5.10</spring-boot.version>
  <openapi.generator.version>7.19.0</openapi.generator.version>

  <jakarta.validation.version>3.1.1</jakarta.validation.version>
  <jakarta.annotation-api.version>3.0.0</jakarta.annotation-api.version>

  <httpclient5.version>5.5.2</httpclient5.version>
  <mockwebserver.version>5.3.2</mockwebserver.version>

  <api-contract.version>0.7.5</api-contract.version>

  <jacoco-maven-plugin.version>0.8.14</jacoco-maven-plugin.version>
  <build.helper.plugin.version>3.6.1</build.helper.plugin.version>

  <maven.compiler.plugin.version>3.14.1</maven.compiler.plugin.version>
  <maven.resources.plugin.version>3.4.0</maven.resources.plugin.version>
  <maven.dependency.plugin.version>3.9.0</maven.dependency.plugin.version>
  <spotless-maven-plugin.version>3.1.0</spotless-maven-plugin.version>
  <maven-surefire-plugin.version>3.5.4</maven-surefire-plugin.version>
  <maven-failsafe-plugin.version>3.5.4</maven-failsafe-plugin.version>

  <openapi.templates.upstream>${project.build.directory}/upstream-templates</openapi.templates.upstream>
  <openapi.templates.effective>${project.build.directory}/effective-templates</openapi.templates.effective>

  <argLine/>
</properties>
```

---

## 2) Core Dependencies

Add these dependencies to your client module.

```xml
<dependencies>

  <!-- Shared contract (single source of truth) -->
  <dependency>
    <groupId>io.github.bsayli</groupId>
    <artifactId>api-contract</artifactId>
    <version>${api-contract.version}</version>
  </dependency>

  <!-- Spring Boot / Spring Web are provided by the host application at runtime -->
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <version>${spring-boot.version}</version>
    <scope>provided</scope>
  </dependency>

  <!-- Optional but explicit (spring-boot-starter-web already brings it transitively) -->
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
    <version>${spring-boot.version}</version>
    <scope>provided</scope>
  </dependency>

  <!-- Used by generated annotations / validation (host app typically provides implementations) -->
  <dependency>
    <groupId>jakarta.validation</groupId>
    <artifactId>jakarta.validation-api</artifactId>
    <version>${jakarta.validation.version}</version>
    <scope>provided</scope>
  </dependency>

  <dependency>
    <groupId>jakarta.annotation</groupId>
    <artifactId>jakarta.annotation-api</artifactId>
    <version>${jakarta.annotation-api.version}</version>
    <scope>provided</scope>
  </dependency>

  <!-- HTTP client (used by RestClient + request factory) -->
  <dependency>
    <groupId>org.apache.httpcomponents.client5</groupId>
    <artifactId>httpclient5</artifactId>
    <version>${httpclient5.version}</version>
  </dependency>

  <!-- Test dependencies -->
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <version>${spring-boot.version}</version>
    <scope>test</scope>
  </dependency>

  <dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>mockwebserver</artifactId>
    <version>${mockwebserver.version}</version>
    <scope>test</scope>
  </dependency>

</dependencies>
```

---

## 3) Maven Plugins — Full Build Pipeline

> Place your Mustache overlays under: `src/main/resources/openapi-templates/`.

```xml
<build>

  <resources>
    <resource>
      <directory>src/main/resources</directory>
      <excludes>
        <exclude>openapi-templates/**</exclude>
      </excludes>
    </resource>
  </resources>

  <plugins>

    <!-- 1) Unpack upstream templates from the OpenAPI Generator JAR -->
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-dependency-plugin</artifactId>
      <version>${maven.dependency.plugin.version}</version>
      <executions>
        <execution>
          <id>unpack-openapi-upstream-templates</id>
          <phase>generate-sources</phase>
          <goals>
            <goal>unpack</goal>
          </goals>
          <configuration>
            <artifactItems>
              <artifactItem>
                <groupId>org.openapitools</groupId>
                <artifactId>openapi-generator</artifactId>
                <version>${openapi.generator.version}</version>
                <type>jar</type>
                <overWrite>true</overWrite>
                <includes>templates/Java/**</includes>
                <outputDirectory>${openapi.templates.upstream}</outputDirectory>
              </artifactItem>
            </artifactItems>
          </configuration>
        </execution>
        <execution>
          <goals>
            <goal>properties</goal>
          </goals>
        </execution>
      </executions>
    </plugin>

    <!-- 2) Copy upstream templates to an effective directory, then overlay local templates -->
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-resources-plugin</artifactId>
      <version>${maven.resources.plugin.version}</version>
      <executions>

        <execution>
          <id>copy-upstream-to-effective</id>
          <phase>generate-sources</phase>
          <goals>
            <goal>copy-resources</goal>
          </goals>
          <configuration>
            <outputDirectory>${openapi.templates.effective}</outputDirectory>
            <resources>
              <resource>
                <directory>${openapi.templates.upstream}/templates</directory>
                <includes>
                  <include>Java/**</include>
                </includes>
              </resource>
            </resources>
          </configuration>
        </execution>

        <execution>
          <id>overlay-local-templates</id>
          <phase>generate-sources</phase>
          <goals>
            <goal>copy-resources</goal>
          </goals>
          <configuration>
            <outputDirectory>${openapi.templates.effective}/Java</outputDirectory>
            <overwrite>true</overwrite>
            <resources>
              <resource>
                <directory>src/main/resources/openapi-templates</directory>
                <filtering>false</filtering>
                <includes>
                  <include>**/*.mustache</include>
                </includes>
              </resource>
            </resources>
          </configuration>
        </execution>

      </executions>
    </plugin>

    <!-- 3) Generate the client using the effective template set -->
    <plugin>
      <groupId>org.openapitools</groupId>
      <artifactId>openapi-generator-maven-plugin</artifactId>
      <version>${openapi.generator.version}</version>

      <executions>
        <execution>
          <id>generate-client</id>
          <phase>generate-sources</phase>
          <goals>
            <goal>generate</goal>
          </goals>

          <configuration>
            <!-- 1) Input OpenAPI spec (your published contract) -->
            <inputSpec>${project.basedir}/src/main/resources/your-api-docs.yml</inputSpec>

            <!-- 2) Generator setup -->
            <generatorName>java</generatorName>
            <library>restclient</library>
            <output>${project.build.directory}/generated-sources/openapi</output>

            <!-- 3) Choose packages for generated sources (adjust for your project) -->
            <!--
              apiPackage    : generated API interfaces / clients
              modelPackage  : generated DTOs (your domain DTOs + thin wrappers)
              invokerPackage: generated HTTP/invoker infrastructure
            -->
            <apiPackage>com.yourcompany.yourapp.client.generated.api</apiPackage>
            <modelPackage>com.yourcompany.yourapp.client.generated.dto</modelPackage>
            <invokerPackage>com.yourcompany.yourapp.client.generated.invoker</invokerPackage>

            <!-- 4) Templates (use your effective/overlay templates) -->
            <templateDirectory>${openapi.templates.effective}/Java</templateDirectory>

            <!-- 5) Generator behavior -->
            <configOptions>
              <useSpringBoot3>true</useSpringBoot3>
              <useJakartaEe>true</useJakartaEe>
              <serializationLibrary>jackson</serializationLibrary>
              <dateLibrary>java8</dateLibrary>
              <useBeanValidation>true</useBeanValidation>
              <openApiNullable>false</openApiNullable>
              <sourceFolder>src/gen/java</sourceFolder>
            </configOptions>

            <!--
              6) api-contract integration
              These keys are consumed by your mustache overlays to import the canonical types
              from api-contract instead of generating duplicates.
            -->
            <additionalProperties>
              <additionalProperty>apiContractEnvelope=io.github.bsayli.apicontract.envelope</additionalProperty>
              <additionalProperty>apiContractPage=io.github.bsayli.apicontract.paging</additionalProperty>
            </additionalProperties>

            <!--
              7) Force specific models to resolve to api-contract classes.
              This is the critical part that makes generated DTOs (e.g. ServiceResponseListX)
              reference Meta/Sort/Page/ServiceResponse from api-contract.
            -->
            <importMappings>
              <importMapping>ServiceResponse=io.github.bsayli.apicontract.envelope.ServiceResponse</importMapping>
              <importMapping>Meta=io.github.bsayli.apicontract.envelope.Meta</importMapping>
              <importMapping>Page=io.github.bsayli.apicontract.paging.Page</importMapping>
              <importMapping>Sort=io.github.bsayli.apicontract.paging.Sort</importMapping>
              <importMapping>SortDirection=io.github.bsayli.apicontract.paging.SortDirection</importMapping>
            </importMappings>

            <!-- 8) Output hygiene -->
            <ignoreFileOverride>${project.basedir}/.openapi-generator-ignore</ignoreFileOverride>
            <cleanupOutput>true</cleanupOutput>
            <skipValidateSpec>false</skipValidateSpec>

            <!-- 9) Disable noise -->
            <generateSupportingFiles>true</generateSupportingFiles>
            <generateApiDocumentation>false</generateApiDocumentation>
            <generateApiTests>false</generateApiTests>
            <generateModelDocumentation>false</generateModelDocumentation>
            <generateModelTests>false</generateModelTests>
          </configuration>
        </execution>
      </executions>
    </plugin>

    <!-- 4) Include generated code in compilation -->
    <plugin>
      <groupId>org.codehaus.mojo</groupId>
      <artifactId>build-helper-maven-plugin</artifactId>
      <version>${build.helper.plugin.version}</version>
      <executions>
        <execution>
          <id>add-generated-sources</id>
          <phase>generate-sources</phase>
          <goals>
            <goal>add-source</goal>
          </goals>
          <configuration>
            <sources>
              <source>${project.build.directory}/generated-sources/openapi/src/gen/java</source>
            </sources>
          </configuration>
        </execution>
      </executions>
    </plugin>

    <!--
      5) Keep generated sources deterministic and clean.
         This step removes unused imports left behind when certain DTOs
         are intentionally ignored in .openapi-generator-ignore
         (e.g. Meta, Page, ServiceResponse).
    -->
    <plugin>
      <groupId>com.diffplug.spotless</groupId>
      <artifactId>spotless-maven-plugin</artifactId>
      <version>${spotless-maven-plugin.version}</version>
      <configuration>
        <java>
          <includes>
            <include>target/generated-sources/openapi/src/gen/java/**/*.java</include>
          </includes>
          <removeUnusedImports>
            <engine>cleanthat-javaparser-unnecessaryimport</engine>
          </removeUnusedImports>
        </java>
      </configuration>
      <executions>
        <execution>
          <id>spotless-apply-generated</id>
          <phase>process-sources</phase>
          <goals>
            <goal>apply</goal>
          </goals>
        </execution>
      </executions>
    </plugin>

  </plugins>
</build>
```

---

## 4) Why These Plugins Exist (and Why This Order Matters)

| Step | Plugin                           | Purpose                                                                   |
| ---- | -------------------------------- |---------------------------------------------------------------------------|
| 1    | `maven-dependency-plugin`        | Extract upstream generator templates as a stable baseline.                |
| 2    | `maven-resources-plugin`         | Overlay local Mustache templates on top of upstream ones.                 |
| 3    | `openapi-generator-maven-plugin` | Generate client sources using the effective (merged) templates.           |
| 4    | `build-helper-maven-plugin`      | Add generated sources to the build without IDE-specific hacks.            |
| 5    | `spotless-maven-plugin`          | Remove unused imports from excluded DTOs to keep output clean and stable. |

---

## 5) Mustache Contract Integration (Template Responsibilities)

Mustache overlays in this project are **not free-form templates**.
They exist to **bind generated client models to the shared `api-contract` types** described by the OpenAPI specification.

Their responsibility is intentionally narrow and declarative.

### Primary responsibility

> **Bind generated wrapper models to the shared `api-contract` types — without redefining response structures.**

Templates do not introduce new response semantics.
They simply reflect what the server already publishes in its OpenAPI contract.

---

### What this means in practice

* Do **not** generate your own `ServiceResponse`, `Meta`, `Page`, or `Sort` classes.
* Do **not** duplicate envelope or paging models already provided by `io.github.bsayli:api-contract`.
* Generated wrappers must **extend** the shared contract types directly.

Wrapper class names are derived from your domain DTOs, but their structure is fixed.

Examples (illustrative only):

* `ServiceResponseFooDto extends ServiceResponse<FooDto>`
* `ServiceResponsePageFooDto extends ServiceResponse<Page<FooDto>)`

The concrete `FooDto` type comes from your service domain.
This document does not assume or prescribe any specific domain model.

---

### How wrapper generation is driven

Wrapper generation is guided entirely by **vendor extensions** emitted in the server-side OpenAPI specification.

Templates **react to** these extensions — they never invent them.

Relevant extensions:

* `x-api-wrapper: true`
  Marks a schema as a response wrapper bound to `ServiceResponse<T>`.

* `x-api-wrapper-datatype: <T>`
  Indicates the domain DTO type wrapped by `ServiceResponse<T>`.

* `x-data-container: Page` *(pagination only)*
  Signals a `ServiceResponse<Page<T>>` wrapper.

* `x-data-item: <T>` *(pagination only)*
  Indicates the item type inside `Page<T>`.

---

### Design scope for generics

Only one nested generic shape is treated as contract-aware:

```java
ServiceResponse<Page<T>>
```

All other generic forms (`List<T>`, `Map<K,V>`, `Foo<Bar>`, etc.) follow OpenAPI Generator’s default
schema naming and model generation behavior.

This keeps client output predictable while covering the most common real-world use case.

---

### Template checklist (summary)

Your Mustache templates should:

* Import `ServiceResponse` from `io.github.bsayli.apicontract.envelope`
* Import `Page` from `io.github.bsayli.apicontract.paging` **only when** pagination metadata is present
* Generate wrapper classes that:

  * extend `ServiceResponse<T>` or `ServiceResponse<Page<T>>`
  * contain **no duplicated envelope or paging logic**

When these responsibilities are respected, client generation remains minimal, stable,
and aligned with the shared API contract.

---

## 6) Verification

After configuring your build and templates, verify the setup by running:

```bash
mvn -q clean install
```

### What to check

Confirm that:

* Generated sources exist under:

  ```
  target/generated-sources/openapi/src/gen/java
  ```

* Wrapper models **extend the shared contract**, for example:

    * `io.github.bsayli.apicontract.envelope.ServiceResponse`

* No duplicated contract DTOs are present in generated output
  *(assuming `.openapi-generator-ignore` is correctly configured)*

If any of the following appear, the setup is **misaligned**:

* Locally generated `ServiceResponse`, `Meta`, or `Page` classes
* Wrapper models that do **not** extend the shared contract
* Multiple envelope implementations across modules

---

✅ With this setup, your client build becomes:

* **Contract-driven** — all success responses rely on the shared `api-contract` artifact
* **Deterministic** — pinned versions and reproducible client generation
* **Template-safe** — Mustache overlays apply minimal, intentional changes only
* **Generics-aware** — explicit and predictable support for `ServiceResponse<Page<T>>`

This section describes the expected bindings used during client generation, not illustrative examples.

If the generated client code or templates diverge from these guidelines, the build may still compile, but it no longer accurately reflects the published OpenAPI contract.