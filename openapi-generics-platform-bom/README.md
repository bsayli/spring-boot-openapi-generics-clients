# openapi-generics-platform-bom

> Internal dependency alignment for the OpenAPI Generics platform

`openapi-generics-platform-bom` is a **Bill of Materials (BOM)** that provides **strict version alignment** across all platform modules and engines.

It ensures the platform runs as a **single, coherent system**—not a collection of independently versioned parts.

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

OpenAPI Generics is a **multi-layered system**:

* contract (authority)
* projection (server)
* generation (client)

Each layer depends on shared engines and must remain compatible.

This BOM exists to:

* centralize version management across all layers
* eliminate cross-module version drift
* ensure compatibility between contract, projection, and generation
* guarantee deterministic and reproducible builds

> Without alignment, the system degrades. With alignment, it behaves predictably.

---

## 🧠 What It Controls

The BOM defines versions for the **minimum set of components required for consistency**.

### Platform Modules

* `openapi-generics-contract`

### Core Engines

* OpenAPI Generator (`openapi-generator`, `openapi-generator-core`)

### Integration Engines

* Springdoc (OpenAPI runtime projection)

Only **version-critical dependencies** are included.

---

## ⚙️ How It Is Used

This BOM is **consumed indirectly**.

It is:

* imported by platform modules
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

End users typically never interact with it directly.

---

## 🚫 Not Intended For Direct Consumption

Consumers of the platform should **not import this BOM manually**.

Instead:

* use `openapi-generics-server-starter` for server-side integration
* use `openapi-generics-java-codegen-parent` for client generation

These entry points already:

* import this BOM
* enforce compatible versions
* expose only the necessary configuration surface

---

## 🔒 Why This Matters

Without a BOM:

* generator versions can diverge across projects
* Springdoc behavior may become inconsistent
* contract compatibility can silently break

With the BOM:

* all modules share a **single version baseline**
* builds become reproducible
* integration behavior becomes predictable

> Version alignment is what makes the platform behave as a system.

---

## 🧩 Architectural Role

Within the platform:

| Layer                      | Module                            |
| -------------------------- | --------------------------------- |
| Contract (Authority)       | `openapi-generics-contract`       |
| Projection (Server)        | `openapi-generics-server-starter` |
| Generation (Client)        | `openapi-generics-java-codegen`   |
| Alignment (Infrastructure) | `openapi-generics-platform-bom`   |

This module exists purely to:

> stabilize the platform dependency graph across all layers

It contains **no runtime behavior** and **no API surface**.

---

## ⚠️ Design Constraints

The BOM is intentionally constrained.

It must:

* remain minimal
* include only version-critical dependencies
* avoid leaking unnecessary transitive dependencies
* stay aligned with platform module versions

This prevents the BOM from becoming a hidden source of complexity.

---

## 🔄 Versioning Strategy

The BOM version represents a **coherent platform snapshot**.

Recommendation:

> All platform modules should use the **same BOM version**.

This ensures:

* contract compatibility
* generator compatibility
* projection consistency

Mixing versions breaks these guarantees.

---

## 🧠 Mental Model

Think of this module as:

> The version control layer of the platform

It defines **what versions are allowed to exist together**.

Not:

* a runtime dependency
* a feature module
* a user-facing API

---

## 📜 License

MIT License

---

**Maintained by:**
**Barış Saylı**
[GitHub](https://github.com/bsayli) · [Medium](https://medium.com/@baris.sayli) · [LinkedIn](https://www.linkedin.com/in/bsayli)
