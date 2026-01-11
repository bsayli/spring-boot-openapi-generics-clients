---
layout: default
title: Client-Side Build Setup
parent: Client-Side Adoption
nav_order: 1
---
# Client-Side Build Setup — Maven Plugins & Dependencies

This guide describes how to configure a **client module build** to generate **type‑safe OpenAPI clients** with **deterministic wrapper typing** and **zero contract duplication**.

**Contract rule (non‑negotiable):**

* All success responses **must** use the shared contract from **`io.github.bsayli:api-contract`**, specifically **`ServiceResponse<T>`**.
* Nested generics are supported **only** for **`ServiceResponse<Page<T>>`**. No other generic nesting is allowed or interpreted.

The build pipeline presented in this document is intentionally explicit and reproducible. At a high level, it:

1. extracts the upstream OpenAPI Generator templates as a stable baseline,
2. overlays your project‑specific Mustache customizations,
3. generates client sources using the effective template set,
4. compiles the generated sources as part of the normal Maven build.

This setup ensures that client generation is **contract‑driven**, **deterministic**, and fully aligned with the server‑side OpenAPI contract.

---

## 📑 Table of Contents

- [1) Core Dependencies](#1-core-dependencies)
- [2) Maven Properties](#2-maven-properties)
- [3) Maven Plugins — Full Build Pipeline](#3-maven-plugins--full-build-pipeline)
- [4) Why These Plugins Exist (and Why This Order Matters)](#4-why-these-plugins-exist-and-why-this-order-matters)
- [5) Mustache Contract Integration (What Your Templates Must Do)](#5-mustache-contract-integration-what-your-templates-must-do)
- [6) Verification](#6-verification)

## 1) Core Dependencies

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

## 2) Maven Properties

Pin versions for deterministic generation and reproducible builds.

```xml
<properties>
  <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
  <java.version>21</java.version>

  <spring-boot.version>3.5.9</spring-boot.version>
  <openapi.generator.version>7.18.0</openapi.generator.version>

  <jakarta.validation.version>3.1.1</jakarta.validation.version>
  <jakarta.annotation-api.version>3.0.0</jakarta.annotation-api.version>

  <httpclient5.version>5.5.2</httpclient5.version>
  <mockwebserver.version>5.3.2</mockwebserver.version>

  <api-contract.version>0.7.4</api-contract.version>

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

            <!-- Your checked-in spec (or fetched by a separate step) -->
            <inputSpec>${project.basedir}/src/main/resources/your-api-docs.yaml</inputSpec>

            <generatorName>java</generatorName>
            <library>restclient</library>
            <output>${project.build.directory}/generated-sources/openapi</output>

            <apiPackage>io.github.bsayli.openapi.client.generated.api</apiPackage>
            <modelPackage>io.github.bsayli.openapi.client.generated.dto</modelPackage>
            <invokerPackage>io.github.bsayli.openapi.client.generated.invoker</invokerPackage>

            <templateDirectory>${openapi.templates.effective}/Java</templateDirectory>

            <generateSupportingFiles>true</generateSupportingFiles>
            <generateApiDocumentation>false</generateApiDocumentation>
            <generateApiTests>false</generateApiTests>
            <generateModelDocumentation>false</generateModelDocumentation>
            <generateModelTests>false</generateModelTests>

            <cleanupOutput>true</cleanupOutput>
            <skipValidateSpec>false</skipValidateSpec>

            <configOptions>
              <useSpringBoot3>true</useSpringBoot3>
              <useJakartaEe>true</useJakartaEe>
              <serializationLibrary>jackson</serializationLibrary>
              <dateLibrary>java8</dateLibrary>
              <useBeanValidation>true</useBeanValidation>
              <openApiNullable>false</openApiNullable>
              <sourceFolder>src/gen/java</sourceFolder>
            </configOptions>

            <!-- Mustache template imports for the shared contract -->
            <additionalProperties>
              <additionalProperty>apiContractEnvelope=io.github.bsayli.apicontract.envelope</additionalProperty>
              <additionalProperty>apiContractPage=io.github.bsayli.apicontract.paging</additionalProperty>
            </additionalProperties>

            <ignoreFileOverride>${project.basedir}/.openapi-generator-ignore</ignoreFileOverride>

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

    <!-- 5) Keep generated sources tidy (optional but recommended) -->
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

| Step | Plugin                           | Why it exists                                                                |
| ---- | -------------------------------- | ---------------------------------------------------------------------------- |
| 1    | `maven-dependency-plugin`        | Extract upstream templates from the generator JAR (baseline).                |
| 2    | `maven-resources-plugin`         | Copy upstream → overlay local Mustache templates (your delta stays minimal). |
| 3    | `openapi-generator-maven-plugin` | Generate client sources with your overlays applied.                          |
| 4    | `build-helper-maven-plugin`      | Compile generated sources without IDE hacks.                                 |
| 5    | `spotless-maven-plugin`          | Optional hygiene: remove unused imports in generated code.                   |

---

## 5) Mustache Contract Integration (What Your Templates Must Do)

Your Mustache overlays are **not free-form templates**. They are part of a **contract-enforcement mechanism** between the server’s OpenAPI output and the client’s generated code.

The primary responsibility of your templates is:

> **Bind generated wrapper models to the shared `api-contract` types — without re‑defining them.**

### What this means in practice

* **Do not generate** your own `ServiceResponse`, `Meta`, `Page`, or `Sort` classes.
* **Do not duplicate** envelope or paging models already provided by `io.github.bsayli:api-contract`.
* **Always extend** the shared contract types in generated wrappers.

### Expected wrapper shape (domain‑agnostic)

Wrapper class names are derived from your **domain DTO names**, but their structure is fixed.

Examples (illustrative only):

* `ServiceResponseFooDto extends ServiceResponse<FooDto>`
* `ServiceResponsePageFooDto extends ServiceResponse<Page<FooDto>>`

> The concrete `FooDto` type comes from *your* service domain.
> This document does **not** assume or enforce any specific domain model.

### How this is enforced

Wrapper generation is driven entirely by **vendor extensions emitted by the server‑side OpenAPI contract**.

Your templates must react to — not invent — these extensions:

* `x-api-wrapper: true`
  Marks a schema as a generated wrapper bound to `ServiceResponse<T>`.

* `x-api-wrapper-datatype: <T>`
  The **raw domain DTO type** wrapped by `ServiceResponse<T>`.

* `x-data-container: Page` *(only for nested generics)*
  Indicates that the wrapper represents `ServiceResponse<Page<T>>`.

* `x-data-item: <T>` *(only for nested generics)*
  The item type inside the `Page<T>` container.

> ⚠️ **Important rule:** Nested generics are supported **only** for `Page<T>`.
> Any other generic form (`List<T>`, `Map<K,V>`, `Foo<Bar>`) is treated as a **raw type** during wrapper generation.

### Template responsibility (summary)

Your Mustache templates must:

* Import `ServiceResponse` from `io.github.bsayli.apicontract.envelope`
* Import `Page` from `io.github.bsayli.apicontract.paging` **only when** `x-data-container` is present
* Generate a wrapper class that:

    * extends `ServiceResponse<T>` or `ServiceResponse<Page<T>>`
    * contains **no duplicated envelope or paging logic**

If these rules are followed, the client build remains **contract‑aligned and future‑proof**.

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

If any of the following appear, the setup is **incorrect**:

* Locally generated `ServiceResponse`, `Meta`, or `Page` classes
* Wrapper models that do **not** extend the shared contract
* Multiple envelope implementations across modules

---

✅ With this setup, your client build becomes:

* **Contract‑driven** — a single, shared `api-contract` artifact
* **Deterministic** — pinned versions and reproducible generation
* **Template‑safe** — overlays apply minimal, intentional deltas
* **Generics‑aware** — with explicit support for `ServiceResponse<Page<T>>`

This section defines **rules**, not examples.
If your templates violate them, the architecture contract is broken — even if the code compiles.
