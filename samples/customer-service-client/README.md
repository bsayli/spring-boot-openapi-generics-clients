# customer-service-client

> **How to generate and integrate a generics-aware OpenAPI client into your own Spring Boot application**

[![Java 21](https://img.shields.io/badge/Java-21-red?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.x-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![OpenAPI Generator](https://img.shields.io/badge/OpenAPI%20Generator-7.21.0-blue?logo=openapiinitiative)](https://openapi-generator.tech/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](../../LICENSE)


---

## 📑 Table of Contents

- 🚀 [TL;DR (Start Here)](#-tldr-start-here)
- 🎯 [What this module is](#-what-this-module-is)
- ❗ [The Problem (Why this exists)](#-the-problem-why-this-exists)
- 💡 [The Approach](#-the-approach)
- 🧠 [How to Use in Your Own Project (Step-by-Step)](#-how-to-use-in-your-own-project-step-by-step)
    - [Step 1 — Add parent (REQUIRED)](#step-1--add-parent-required)
    - [Step 2 — Provide OpenAPI spec](#step-2--provide-openapi-spec)
    - [Step 3 — Configure generator](#step-3--configure-generator)
    - [Step 4 — Build](#step-4--build)
    - [Step 5 — Integrate (IMPORTANT)](#step-5--integrate-important)
- 🧩 [Adapter Pattern (Recommended)](#-adapter-pattern-recommended)
- 🌐 [HTTP Client Setup (Production Ready)](#-http-client-setup-production-ready)
- ⚖️ [Error Handling Model](#-error-handling-model)
- 🧬 [Supported Contract Scope](#-supported-contract-scope)
- 🏗️ [What Actually Controls Generation](#-what-actually-controls-generation)
- 🔗 [Related Modules](#-related-modules)
- 🧪 [Testing](#-testing)
- 🛡️ [License](#-license)
- 🧾 [Final Note](#-final-note)

---

## 🚀 TL;DR (Start Here)

If you just want to use this in your own project:

### 1. Inherit the parent

```xml
<parent>
  <groupId>io.github.bsayli</groupId>
  <artifactId>openapi-generics-java-codegen-parent</artifactId>
  <version>0.8.0-SNAPSHOT</version>
</parent>
```

### 2. Add your OpenAPI spec

```text
src/main/resources/my-api.yaml
```

### 3. Configure generator (minimal)

```xml
<plugin>
  <groupId>org.openapitools</groupId>
  <artifactId>openapi-generator-maven-plugin</artifactId>

  <executions>
    <execution>
      <goals>
        <goal>generate</goal>
      </goals>
      <configuration>
        <inputSpec>${project.basedir}/src/main/resources/my-api.yaml</inputSpec>
        <library>restclient</library>
      </configuration>
    </execution>
  </executions>
</plugin>
```

### 4. Generate client

```bash
mvn clean install
```

### 5. Use generated API

```java
MyApi api = ...
```

---

## 🎯 What this module is

This module is a **reference consumer implementation**.

It shows how to:

* generate a client from a generics-aware OpenAPI spec
* preserve `ServiceResponse<T>` semantics
* integrate the generated client into a real Spring Boot app

> This is not a reusable SDK.
> This is a **working integration blueprint**.

---

## ❗ The Problem (Why this exists)

Standard OpenAPI client generation:

* duplicates envelope models
* loses generic type semantics
* produces unstable code across versions
* couples business logic to generated classes

Result:

* fragile integrations
* regeneration pain
* poor type safety

---

## 💡 The Approach

This project demonstrates a **contract-aligned generation + integration model**:

```text
OpenAPI (with semantics)
        ↓
Controlled code generation
        ↓
Thin wrapper models
        ↓
Adapter boundary
        ↓
Application usage
```

Key ideas:

* contract types are reused (not generated)
* wrappers are generated as thin inheritance
* generated code is isolated behind an adapter
* runtime behavior is explicitly configured

---

## 🧠 How to Use in Your Own Project (Step-by-Step)

### Step 1 — Add parent (REQUIRED)

This enables deterministic generation.

```xml
<parent>
  <groupId>io.github.bsayli</groupId>
  <artifactId>openapi-generics-java-codegen-parent</artifactId>
  <version>0.8.0-SNAPSHOT</version>
</parent>
```

---

### Step 2 — Provide OpenAPI spec

Place your spec:

```text
src/main/resources/my-api.yaml
```

Important:

* must include vendor extensions (`x-api-wrapper` etc.)
* must be produced by compatible server (or manually aligned)

---

### Step 3 — Configure generator

Minimal configuration only:

```xml
<plugin>
  <groupId>org.openapitools</groupId>
  <artifactId>openapi-generator-maven-plugin</artifactId>

  <executions>
    <execution>
      <phase>generate-sources</phase>
      <goals>
        <goal>generate</goal>
      </goals>

      <configuration>
        <inputSpec>${project.basedir}/src/main/resources/my-api.yaml</inputSpec>
        <library>restclient</library>
      </configuration>
    </execution>
  </executions>
</plugin>
```

Do NOT configure:

* templates
* generatorName
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

Do NOT use generated APIs directly.

Instead:

```text
Application → Adapter → Generated API
```

---

## 🧩 Adapter Pattern (Recommended)

### Why?

Generated code changes.
Your application should not.

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
* no retries
* custom error handling

You can reuse or simplify this setup depending on your needs.

---

## ⚖️ Error Handling Model

Errors use **ProblemDetail (RFC 9457)**.

Behavior:

* server returns structured error
* client parses it
* wrapped into `ApiProblemException`

Fallbacks:

* empty body
* non-JSON
* unparsable

All produce deterministic error objects.

---

## 🧬 Supported Contract Scope

Supported:

* `ServiceResponse<T>`
* `ServiceResponse<Page<T>>`

Not supported:

* arbitrary nested generics
* maps
* complex wrappers

This is intentional for determinism.

---

## 🏗️ What Actually Controls Generation

Important mental model:

```text
Parent (behavior)
+ Templates (logic)
+ Plugin (execution)
+ Spec (input)
```

NOT just:

```text
OpenAPI Generator plugin
```

---

## 🔗 Related Modules

* **[api-contract](../../api-contract/README.md)**  
  Canonical response contract (authority layer).

* **[customer-service](../customer-service/README.md)**  
  Sample producer demonstrating contract-first API exposure.

* **[openapi-generics-java-codegen-parent](../../openapi-generics-java-codegen-parent/README.md)**  
  Build-time orchestration layer.

* **[openapi-generics-java-codegen](../../openapi-generics-java-codegen/README.md)**  
  Custom generator enforcing contract-aware client generation.

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

This module is not about "generating clients".

It is about:

> Generating **correct, stable, contract-aligned clients** and integrating them safely into real applications.
