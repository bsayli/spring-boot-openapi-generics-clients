# openapi-generics-platform-bom

> Internal dependency alignment for the OpenAPI Generics platform

`openapi-generics-platform-bom` is a **Bill of Materials (BOM)** that provides **version alignment** across all platform modules and engines.

It ensures that the ecosystem operates in a **deterministic, compatible, and reproducible configuration**.

---

## Table of Contents

1. [Purpose](#-purpose)
2. [What It Controls](#-what-it-controls)
3. [How It Is Used](#-how-it-is-used)
4. [Not Intended For Direct Consumption](#-not-intended-for-direct-consumption)
5. [Why This Matters](#-why-this-matters)
6. [Architectural Role](#-architectural-role)
7. [Design Constraints](#-design-constraints)
8. [Versioning Strategy](#-versioning-strategy)
9. [Mental Model](#-mental-model)
10. [License](#-license)

---

## 🎯 Purpose

This BOM exists to:

* centralize version management across platform modules
* ensure compatibility between server, contract, and codegen layers
* eliminate dependency drift
* guarantee deterministic builds

It is part of the platform’s **infrastructure layer**, not its public API.

---

## 🧠 What It Controls

The BOM defines versions for:

### Platform Modules

* `openapi-generics-contract`

### Core Engines

* OpenAPI Generator (`openapi-generator`, `openapi-generator-core`)

### Integration Engines

* Springdoc (OpenAPI runtime integration)

---

## ⚙️ How It Is Used

This BOM is **not intended to be used directly by end users**.

Instead, it is:

* imported internally by platform modules
* used by parent POMs
* propagated transitively to consumers

Example (internal usage):

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.github.blueprintplatform</groupId>
      <artifactId>openapi-generics-platform-bom</artifactId>
      <version>0.8.x</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

---

## 🚫 Not Intended For Direct Consumption

Consumers of the platform should **not** import this BOM manually.

Instead:

* use `openapi-generics-server-starter` for server-side usage
* use `openapi-generics-java-codegen-parent` for client generation

These modules already manage dependency alignment internally.

---

## 🔒 Why This Matters

Without a BOM:

* generator version mismatches may occur
* Springdoc behavior may drift
* contract compatibility may break

With the BOM:

> All platform components operate on a **single, controlled version set**

---

## 🧩 Architectural Role

Within the platform:

| Layer                      | Module                            |
| -------------------------- | --------------------------------- |
| Contract (Authority)       | `openapi-generics-contract`                    |
| Projection (Server)        | `openapi-generics-server-starter` |
| Generation (Client)        | `openapi-generics-java-codegen`   |
| Alignment (Infrastructure) | `openapi-generics-platform-bom`   |

This module exists purely to:

> stabilize the platform dependency graph

---

## ⚠️ Design Constraints

The BOM must:

* remain minimal
* only include version-critical dependencies
* avoid leaking unnecessary transitive dependencies

---

## 🔄 Versioning Strategy

The BOM version tracks platform compatibility.

Recommendation:

> All platform modules should use the **same BOM version**

---

## 🧠 Mental Model

Think of this module as:

> The version control layer of the platform

Not:

* a runtime component
* a feature module
* a user-facing dependency

---

## 📜 License

MIT License

---

**Maintained by:**
Barış Saylı
[https://github.com/bsayli](https://github.com/bsayli)
