# Contributing Guide

Thanks for your interest in improving **spring-boot-openapi-generics-clients**!
This repository demonstrates a **contract-driven, generics-aware approach to OpenAPI client generation**, built on **Java 21**, **Spring Boot 3.5.x**, and **OpenAPI Generator 7.x**, centered around a *single canonical response contract* and **RFC 9457 Problem Details**.

> Be kind. Be constructive. See our [Code of Conduct](./CODE_OF_CONDUCT.md).

---

## Table of contents

* [Questions & support](#questions--support)
* [How to contribute](#how-to-contribute)
* [Development setup](#development-setup)
* [Project layout](#project-layout)
* [Coding style & commits](#coding-style--commits)
* [Testing & coverage](#testing--coverage)
* [OpenAPI spec & client generation](#openapi-spec--client-generation)
* [Pull Request checklist](#pull-request-checklist)
* [Labels we use](#labels-we-use)
* [Security](#security)
* [License](#license)

---

## Questions & Support

Have a question, design idea, or usage concern?

- Use **GitHub Discussions → Ideas** for:
    - API contract design
    - response envelope patterns
    - generics handling (e.g. `ServiceResponse<T>`, `ServiceResponse<Page<T>>`)

- Use **GitHub Discussions → Q&A** for:
    - setup and configuration questions
    - client generation issues
    - integration or usage problems

* Found a bug or regression?

    * Open an **Issue** with a minimal reproduction and clear expected vs actual behavior.

Please search existing issues and discussions before opening a new one.

---

## How to contribute

1. **Fork** the repository and create a feature branch:

   ```bash
   git checkout -b feature/scope-short-title
   ```

2. Make **small, focused changes** aligned with the project’s contract‑first philosophy.

3. Add or update **tests and documentation** where behavior or guarantees change.

4. Run the full build locally (see below).

5. Open a PR against `main` with a clear description and checklist.

> 💡 Small, well‑scoped PRs are reviewed faster and are easier to reason about.

---

## Development setup

### Prerequisites

* **Java 21** (Temurin recommended)
* **Maven 3.9+**
* **Docker** (optional; only if you want to containerize/run the server)

### Repository build model (important)

This repository is a **multi‑module Maven aggregator**.
At the repository root you have a parent `pom.xml` (`packaging=pom`) that defines the module order:

* `api-contract`
* `customer-service`
* `customer-service-client`

**Use the root build by default.** It guarantees the correct build order and ensures the shared contract is available to downstream modules.

### Build everything (recommended)

```bash
# From repository root
mvn -q -ntp clean verify
```

### Build a single module (when you need it)

From repository root:

```bash
# Only build and test the server module (and any required dependencies)
mvn -q -ntp -pl customer-service -am clean verify

# Only build and test the client module (and any required dependencies)
mvn -q -ntp -pl customer-service-client -am clean verify
```

> `-am` (**also-make**) ensures Maven builds `api-contract` first when required.

### Run the server locally

```bash
cd customer-service
mvn -q -ntp spring-boot:run
# http://localhost:8084/customer-service
```

---

## Project layout

```
/api-contract               # Shared contract: ServiceResponse<T>, Page<T>, Meta, Sort, RFC 9457 helpers
/customer-service           # Spring Boot API producer (publishes OpenAPI 3.1 spec)
/customer-service-client    # OpenAPI-generated Java client (RestClient + Mustache overlays)
/docs                       # Adoption guides & GitHub Pages sources
/.github/workflows          # CI pipelines (build, tests, artifacts, Codecov)
/CODE_OF_CONDUCT.md         # Community standards
/README.md                  # Entry point documentation
```

---

## Coding style & commits

### Java style

* Keep code **idiomatic and minimal**.
* Favor **small, reviewable changes**.
* Do **not** manually edit generated sources under `target/generated-sources/...`.
  If the generated output must change, change **templates**, **schema customizers**, or **contract types**.

### Commit convention

Use clear, descriptive prefixes to communicate intent:

* `feature:` — new capability or extension
* `bugfix:` — defect or regression fix
* `docs:` — documentation‑only changes
* `chore:` — build, dependency, or tooling updates
* `refactor:` — internal restructuring without behavior change
* `test:` — test additions or corrections
* `ci:` — CI or workflow changes

**Examples:**

* `feature(client): support Page-only nested generics`
* `bugfix(server): guard null composed schemas during introspection`
* `docs(adoption): clarify api-contract ownership`

Favor **clarity over cleverness**; commits should be easy to review in isolation.

---

## Testing & coverage

### Run the full test suite

```bash
# From repository root
mvn -q -ntp clean verify
```

### Typical focused runs

```bash
# Server only
mvn -q -ntp -pl customer-service -am clean verify

# Client only
mvn -q -ntp -pl customer-service-client -am clean verify
```

Notes:

* CI uploads coverage via **Codecov**.
* Client tests commonly use **MockWebServer** to verify:

    * `{ data, meta }` envelope parsing
    * RFC 9457 `ProblemDetail` decoding
    * HTTP status propagation

---

## OpenAPI Spec & Client Generation

This section explains how the OpenAPI specification is produced and consumed, and how client generation stays aligned with the shared API contract.

---

## Core Assumptions

The client module is **not self-contained**. It is designed to compile against a shared, pre-defined contract:

* `ServiceResponse<T>` is provided by **`io.github.bsayli:api-contract`**
* Nested generics are considered contract-aware **only** for:

  ```java
  ServiceResponse<Page<T>>
  ```

As a result, client generation assumes that the contract module is already available on the classpath.

---

## Recommended Workflow: Generate from the Live Server Spec

The preferred approach is to generate clients from the OpenAPI document produced by the running server.

### 1. Start the server

```bash
cd customer-service
mvn -q -ntp spring-boot:run
```

### 2. Fetch the generated OpenAPI specification

```bash
cd ../customer-service-client
curl -s http://localhost:8084/customer-service/v3/api-docs.yaml \
  -o src/main/resources/customer-api-docs.yaml
```

This specification already includes:

* composed wrapper schemas
* vendor extensions for generic binding
* pagination hints where applicable

### 3. Build the client from the repository root

```bash
cd ..
mvn -q -ntp -pl customer-service-client -am clean verify
```

Building from the root ensures:

* `api-contract` is built first
* the client compiles against the exact same contract version
* no accidental divergence between modules

---

## Alternative Workflow: Using a Committed Spec

It is also possible to work against a committed OpenAPI file:

* Edit or replace:

  ```
  customer-service-client/src/main/resources/customer-api-docs.yaml
  ```
* Build using the aggregator or `-pl ... -am`

This is useful for documentation, reviews, or offline work.

> The authoritative OpenAPI document is still the one generated by the server.
> When using a committed spec, contributors should ensure it reflects the current server behavior to avoid contract drift.

---

## Client Generation Rules (Template-Level)

Client templates are intentionally minimal and declarative. Their behavior is defined as follows:

* All success wrappers **extend** `ServiceResponse<T>` from `api-contract`
* Wrapper models are thin type binders; they do not redefine envelope fields
* Nested generics are handled explicitly **only** for:

  ```java
  ServiceResponse<Page<T>>
  ```

Other generic shapes (e.g. `List<T>`, `Map<K,V>`) follow OpenAPI Generator’s default behavior.

This keeps schema naming deterministic and generation stable across versions.

---

## Avoiding Duplicate Models

The `.openapi-generator-ignore` file excludes models that already exist in the shared contract module.

Examples:

* `ServiceResponse`
* `Meta`
* `Page`
* `Sort`, `SortDirection`

As a result:

* the response envelope is defined **once**
* generated code focuses only on binding type parameters
* server and client remain aligned by construction

---

## Summary

* The server publishes a contract-aware OpenAPI specification
* The client consumes that specification while reusing the shared contract
* Generation is deterministic because responsibilities are clearly separated

> **One contract, one definition — shared across the boundary.**

---

## Pull Request checklist

Before opening a PR:

* [ ] Scope is minimal and focused
* [ ] `mvn -q -ntp clean verify` passes locally
* [ ] Tests added or updated where behavior changes
* [ ] Documentation updated if guarantees or usage change
* [ ] No accidental edits to generated code outside intended overlays
* [ ] Title uses a clear prefix (`feature:`, `bugfix:`, `docs:` …)

---

## Labels we use

* **`enhancement`** — new capability or meaningful DX improvement
* **`bug`** — incorrect behavior or broken generation edge case
* **`documentation`** — README, guides, or examples
* **`good first issue`** — small, well‑scoped starter tasks
* **`help wanted`** — community contributions welcome
* **`discussion needed`** — requires design consensus before implementation

---

## Security

If you discover a **security vulnerability**, **do not** open a public issue.

* Preferred: **GitHub Security Advisory** (Security → Advisories → Report a vulnerability)
* Or email **[baris.sayli@gmail.com](mailto:baris.sayli@gmail.com)** with subject:

  ```text
  SECURITY: <short summary>
  ```

See [SECURITY.md](./SECURITY.md) for the full policy.

---

## License

This project is licensed under the **MIT License** (see [LICENSE](./LICENSE)).
By contributing, you agree that your contributions are licensed under the same terms.
