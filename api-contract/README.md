# api-contract

> **Single source of truth for API response, pagination, and error contracts.**

`api-contract` is a **pure contract module** that defines the canonical response and paging model shared by **both server and client**.

It intentionally contains **no framework dependencies** (no Spring, no HTTP, no OpenAPI logic) and is designed to be:

* stable
* reusable
* generator‑friendly
* language‑agnostic

This module is the **foundation** of the entire architecture.

---

## 📑 Table of Contents

* 🎯 [Purpose](#-purpose)
* 🧱 [Core Types](#-core-types)
  * [ServiceResponse<T>](#serviceresponset)
  * [Meta](#meta)
  * [Pagination](#pagination)
    * [Page<T>](#paget)
  * [Sorting](#sorting)
* ⚠️ [Error Contracts (RFC 9457)](#-error-contracts-rfc9457)
  * [ProblemExtensions](#problemextensions)
  * [ErrorItem](#erroritem)
* 🚫 [What This Module Does Not Do](#-what-this-module-does-not-do)
* 🧠 [Architectural Rules](#-architectural-rules)
* 📦 [Dependency Usage](#-dependency-usage)
* 🔐 [Versioning & Stability](#-versioning--stability)
* 📄 [License](#-license)

---

## 🎯 Purpose

Modern APIs almost always wrap responses with metadata:

* timestamps
* pagination
* sorting
* structured errors

The problem is not wrapping — the problem is **duplicating those wrappers** across server and client.

`api-contract` solves this by providing **one canonical contract** that:

* servers **return directly**
* clients **extend**, never re‑generate

> There must be **exactly one** definition of the response envelope.

---

## 🧱 Core Types

### `ServiceResponse<T>`

Canonical success envelope used everywhere:

```java
public class ServiceResponse<T> {
  private T data;
  private Meta meta;
}
```

Usage:

```java
return ServiceResponse.of(customerDto);
```

Key properties:

* Generic payload: `T`
* Metadata always present (`Meta.now()` by default)
* No framework annotations

---

### `Meta`

Common metadata attached to every successful response.

```java
public record Meta(
    Instant serverTime,
    List<Sort> sort
) {}
```

Design goals:

* request‑level context
* extensible without breaking clients
* deterministic serialization

---

### Pagination

#### `Page<T>`

Language‑agnostic pagination container:

```java
public record Page<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean hasNext,
    boolean hasPrev
) {}
```

Important rule:

> **The only supported nested generic is:**
>
> ```java
> ServiceResponse<Page<T>>
> ```

This constraint is intentional and guarantees deterministic OpenAPI schemas and client generation.

---

### Sorting

```java
public record Sort(String field, SortDirection direction) {}
```

```java
public enum SortDirection {
  ASC, DESC
}
```

Used exclusively inside `Meta`.

---

## ⚠️ Error Contracts (RFC 9457)

### `ProblemExtensions`

Optional extension container for `application/problem+json`:

```java
public record ProblemExtensions(
    List<ErrorItem> errors
) {}
```

### `ErrorItem`

```java
public record ErrorItem(
    String code,
    String message,
    String field,
    String resource,
    String id
) {}
```

Design principles:

* fully RFC 9457‑compatible
* additive and forward‑compatible
* no transport or framework coupling

---

## 🚫 What This Module Does **Not** Do

This module intentionally does **not**:

* depend on Spring or Spring Boot
* publish OpenAPI schemas
* perform validation or HTTP mapping
* contain controllers or adapters
* generate client code

Those concerns belong to **other modules**.

---

## 🧠 Architectural Rules

These rules are **non‑negotiable**:

1. **One envelope** — `ServiceResponse<T>`
2. **No duplication** — clients extend, never re‑define
3. **Page‑only nested generics** — nothing else
4. **Contracts evolve carefully** — breaking changes are explicit

If any module violates these rules, the architecture is broken.

---

## 📦 Dependency Usage

Add to both server and client modules:

```xml
<dependency>
  <groupId>io.github.bsayli</groupId>
  <artifactId>api-contract</artifactId>
  <version>0.7.4</version>
</dependency>
```

Scope recommendations:

* **server**: `compile`
* **client**: `compile` or `provided` (depending on packaging)

---

## 🔐 Versioning & Stability

* This project is **pre‑1.0**
* Contracts may evolve, but:

    * changes are intentional
    * documented
    * reflected across server & client

Always upgrade **all modules together**.

---

## 📄 License

MIT License.

This module defines **contracts**, not behavior.
Once published, those contracts become promises.

> **Architecture starts with contracts.**
> `api-contract` is where that promise lives.
