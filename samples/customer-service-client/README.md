# customer-service-client

> **Reference integration: generating and using a contract-aligned, generics-aware OpenAPI client in a Spring Boot application**

[![Java 21](https://img.shields.io/badge/Java-21-red?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.x-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![OpenAPI Generator](https://img.shields.io/badge/OpenAPI%20Generator-7.x-blue?logo=openapiinitiative)](https://openapi-generator.tech/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](../../LICENSE)

---

## 📑 Table of Contents

* 🚀 [TL;DR (Start Here)](#-tldr-start-here)
* 🎯 [What this module is](#-what-this-module-is)
* ❗ [The Problem (Why this exists)](#-the-problem-why-this-exists)
* 💡 [The Approach](#-the-approach)
* 🧠 [How to Use in Your Own Project (Step-by-Step)](#-how-to-use-in-your-own-project-step-by-step)
  * [Step 1 — Add parent (REQUIRED)](#step-1--add-parent-required)
  * [Step 2 — Provide OpenAPI spec](#step-2--provide-openapi-spec)
  * [Step 3 — Configure generator](#step-3--configure-generator)
  * [Step 4 — Build](#step-4--build)
  * [Step 5 — Integrate (IMPORTANT)](#step-5--integrate-important)
* 🧩 [Adapter Pattern (Recommended)](#-adapter-pattern-recommended)
* 🌐 [HTTP Client Setup (Production Ready)](#-http-client-setup-production-ready)
* ⚖️ [Error Handling Model](#-error-handling-model)
* 🧬 [Supported Contract Scope](#-supported-contract-scope)
* 🏗️ [What Actually Controls Generation](#-what-actually-controls-generation)
* 🔗 [Related Modules](#-related-modules)
* 🧪 [Testing](#-testing)
* 🛡️ [License](#-license)
* 🧾 [Final Note](#-final-note)

---

## 🚀 TL;DR (Start Here)

If you just want a working, correct client:

### 1. Inherit the parent

```xml
<parent>
  <groupId>io.github.blueprintplatform</groupId>
  <artifactId>openapi-generics-java-codegen-parent</artifactId>
  <version>0.8.2</version>
</parent>
```

### 2. Add your OpenAPI spec

```text
src/main/resources/your-api-docs.yaml
```

### 3. Configure generator (minimal)

```xml
<plugin>
  <groupId>org.openapitools</groupId>
  <artifactId>openapi-generator-maven-plugin</artifactId>

  <executions>
    <execution>
      <id>generate-client</id>
      <phase>generate-sources</phase>
      <goals>
        <goal>generate</goal>
      </goals>

      <configuration>

        <inputSpec>${project.basedir}/src/main/resources/your-api-docs.yaml</inputSpec>

        <library>your-library-choice</library>
        <apiPackage>your.api.package</apiPackage>
        <modelPackage>your.model.package</modelPackage>
        <invokerPackage>your.invoker.package</invokerPackage>

        <configOptions>
          <useSpringBoot3>true</useSpringBoot3>
          <serializationLibrary>your-choice</serializationLibrary>
          <openApiNullable>false</openApiNullable>
        </configOptions>

      </configuration>

    </execution>
  </executions>
</plugin>
```


### 4. Generate client

```bash
mvn clean install
```

### 5. Use it via adapter

The generated client should never be used directly from application code.

Instead, introduce a thin adapter that:

* defines a stable interface for your application
* delegates to generated APIs
* keeps contract types (`ServiceResponse<T>`) intact

Minimal usage looks like:

```java
customerClient.getCustomer(id);
```

Under the hood, this is backed by an adapter layer:

```java
public interface CustomerClientAdapter {
  ServiceResponse<CustomerDto> getCustomer(Integer customerId);
}
```

```java
@Service
public class CustomerClientAdapterImpl implements CustomerClientAdapter {

  private final CustomerControllerApi api;

  public CustomerClientAdapterImpl(CustomerControllerApi api) {
    this.api = api;
  }

  @Override
  public ServiceResponse<CustomerDto> getCustomer(Integer customerId) {
    return api.getCustomer(customerId);
  }
}
```

Key idea:

> The adapter owns the integration boundary. Generated code stays behind it.

Implication:

* you can regenerate clients safely
* your application remains stable
* contract types flow through unchanged

---

## 🎯 What this module is

This module is a **reference consumer implementation**.

It shows how to:

* generate a client from a generics-aware OpenAPI spec
* preserve `ServiceResponse<T>` semantics
* integrate the generated client safely into a Spring Boot application

> This is not a reusable SDK.
> This is a **correct integration model**.

---

## ❗ The Problem (Why this exists)

Default OpenAPI client generation:

* duplicates envelope models
* loses generic type semantics
* produces unstable outputs across builds
* leaks generated models into application code

Result:

* regeneration breaks
* type safety degrades
* contract drifts between server and client

---

## 💡 The Approach

OpenAPI is treated as transport — not as the contract.

This module demonstrates a **contract-aligned generation model**:

```text
OpenAPI (projection)
        ↓
Controlled build pipeline
        ↓
Thin wrapper models
        ↓
Adapter boundary
        ↓
Application usage
```

Key principles:

* contract types are reused — not generated
* wrappers are structural — not behavioral
* generation is deterministic
* application code is isolated from generated code

---

## 🧠 How to Use in Your Own Project (Step-by-Step)

### Step 1 — Add parent (REQUIRED)

This activates the generation system.

```xml
<parent>
  <groupId>io.github.blueprintplatform</groupId>
  <artifactId>openapi-generics-java-codegen-parent</artifactId>
  <version>0.8.2</version>
</parent>
```

---

### Step 2 — Provide OpenAPI spec

```text
src/main/resources/your-api-docs.yaml
```

The spec must:

* must be produced by a compatible server (vendor extensions are required)

---

### Step 3 — Configure generator

Minimal configuration only:

```xml
<plugin>
  <groupId>org.openapitools</groupId>
  <artifactId>openapi-generator-maven-plugin</artifactId>

  <executions>
    <execution>
      <id>generate-client</id>
      <phase>generate-sources</phase>
      <goals>
        <goal>generate</goal>
      </goals>

      <configuration>

        <inputSpec>${project.basedir}/src/main/resources/your-api-docs.yaml</inputSpec>

        <library>your-library-choice</library>
        <apiPackage>your.api.package</apiPackage>
        <modelPackage>your.model.package</modelPackage>
        <invokerPackage>your.invoker.package</invokerPackage>

        <configOptions>
          <useSpringBoot3>true</useSpringBoot3>
          <serializationLibrary>your-choice</serializationLibrary>
          <openApiNullable>false</openApiNullable>
        </configOptions>

      </configuration>

    </execution>
  </executions>
</plugin>
```

Do NOT configure:

* generatorName
* templates
* importMappings

These are controlled by the parent.

---

### Step 4 — Build

```bash
mvn clean install
```

Generated sources:

```text
target/generated-sources/openapi/src/gen/java
```

---

### Step 5 — Integrate (IMPORTANT)

Never expose generated APIs directly.

```text
Application → Adapter → Generated API
```

---

## 🧩 Adapter Pattern (Recommended)

### Why

Generated code is replaceable.
Your application should not be.

The adapter is the stability boundary of your system.

---

### Example

```java
public interface CustomerClientAdapter {
  ServiceResponse<CustomerDto> getCustomer(Integer id);
}
```

```java
@Service
public class CustomerClientAdapterImpl implements CustomerClientAdapter {

  private final CustomerControllerApi api;

  public CustomerClientAdapterImpl(CustomerControllerApi api) {
    this.api = api;
  }

  @Override
  public ServiceResponse<CustomerDto> getCustomer(Integer id) {
    return api.getCustomer(id);
  }
}
```

---

## 🌐 HTTP Client Setup (Production Ready)

This module demonstrates:

* Apache HttpClient 5
* connection pooling
* timeouts
* explicit behavior (no hidden retries)

You may simplify or replace this depending on your environment.

---

## ⚖️ Error Handling Model

Errors follow a runtime protocol:

```text
ProblemDetail (RFC 9457)
```

Behavior:

* parsed into structured objects
* surfaced via `ApiProblemException`

Fallbacks handled:

* empty response
* invalid JSON
* unexpected formats

---

## 🧬 Supported Contract Scope

Supported:

* `ServiceResponse<T>`
* `ServiceResponse<Page<T>>`

Out of scope:

* arbitrary nested generics
* maps
* custom wrappers

Reason:

> determinism over flexibility

---

## 🏗️ What Actually Controls Generation

Mental model:

```text
Parent (orchestration)
+ Templates (structure)
+ Generator (rules)
+ Spec (input)
```

NOT just:

```text
OpenAPI Generator plugin
```

---

## 🔗 Related Modules

* **[openapi-generics-contract](../../openapi-generics-contract/README.md)**
  Canonical response contract.

* **[customer-service](../customer-service/README.md)**
  Producer reference.

* **[openapi-generics-java-codegen-parent](../../openapi-generics-java-codegen-parent/README.md)**
  Build-time orchestration.

* **[openapi-generics-java-codegen](../../openapi-generics-java-codegen/README.md)**
  Generator enforcement layer.

---

## 🧪 Testing

```bash
mvn verify
```

---

## 🛡️ License

MIT License

---

## 🧾 Final Note

This module is not about generating clients.

It is about:

> Generating **deterministic, contract-aligned clients** and integrating them safely.

---

**Maintained by:**
**Barış Saylı**
[GitHub](https://github.com/bsayli) · [Medium](https://medium.com/@baris.sayli) · [LinkedIn](https://www.linkedin.com/in/bsayli)
