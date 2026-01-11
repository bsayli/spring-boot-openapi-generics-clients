# Contributing Guide

Thanks for your interest in improving **spring-boot-openapi-generics-clients**!
This project showcases **type-safe, generics-aware OpenAPI clients** (Java 21, Spring Boot 3.5.x, OpenAPI Generator 7.x) with a `{ data, meta }` envelope and RFC 9457 Problem Details.

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

## Questions & support

* Have a question or idea?

    * Use **GitHub Discussions → *Ideas*** for design/roadmap talk (e.g., envelopes, nested containers).
    * Use **Discussions → *Q&A*** for “how do I…?” support.
* Found a bug? Open an **Issue** with a minimal repro.

Please search existing issues/discussions before opening a new one.

---

## How to contribute

1. **Fork** the repo and create a feature branch:

   ```bash
   git checkout -b feature/scope-short-title
   ```
2. Make small, focused changes.
3. Add/adjust tests and docs where it makes sense.
4. Run the full build locally (see below).
5. Open a PR to `main` with a clear description and checklist.

> Tip: Small PRs with a tight scope are reviewed faster.

---

## Development setup

**Prereqs**

* Java 21 (Temurin recommended)
* Maven 3.9+
* Docker (optional; for running the service in a container)

**Build everything**

```bash
# From repo root
mvn -q -ntp clean verify
```

**Run server locally**

```bash
cd customer-service
mvn spring-boot:run
# http://localhost:8084/customer-service
```

**Generate & build client locally**

```bash
cd customer-service-client
# Make sure the server is running so the spec is reachable (or place your spec under src/main/resources)
mvn -q clean install
```

---

## Project layout

```
/customer-service            # Spring Boot API producer (exposes OpenAPI 3.1)
/customer-service-client     # Generated Java client (RestClient + overlays)
/docs                        # Adoption guides & GitHub Pages sources
/.github/workflows           # CI (build, tests, Codecov)
/CODE_OF_CONDUCT.md          # Community standards
/README.md                   # Root docs (start here)
```

---

## Coding style & commits

* **Java style**: keep it idiomatic and consistent with **Google Java Format**.  
  Run your formatter locally if you touch code outside generated folders.

* **Commit convention**:  
  Use clear, descriptive prefixes for consistency and readability.  
  Recommended prefixes:

    * `feature:` — for new features or enhancements
    * `bugfix:` — for bug fixes
    * `docs:` — for documentation changes
    * `chore:` — for maintenance or configuration updates
    * `refactor:` — for internal code restructuring
    * `test:` — for adding or updating tests
    * `ci:` — for continuous integration or workflow changes

  **Examples:**

    * `feature(client): support configurable container allow-list`
    * `bugfix(server): avoid NPE in schema enrichment for composed types`

* Favor clear naming, small classes, and cohesive tests that verify a single behavior.
---

## Testing & coverage

Run unit + integration tests:

```bash
# Module tests
cd customer-service && mvn -q clean verify
cd ../customer-service-client && mvn -q clean verify
```

* Coverage is uploaded by CI via **Codecov** (see action config).
* Client tests often use **MockWebServer** to verify Problem Details handling and envelope parsing.

---

## OpenAPI spec & client generation

There are two common flows:

### 1) Use the live spec from the local server

1. Run `customer-service`.
2. Pull the spec into the client module:

   ```bash
   cd customer-service-client
   curl -s http://localhost:8084/customer-service/v3/api-docs.yaml \
     -o src/main/resources/customer-api-docs.yaml
   ```
3. Generate & compile:

   ```bash
   mvn -q clean install
   ```

### 2) Work against a committed spec

* Edit or replace `customer-service-client/src/main/resources/customer-api-docs.yaml`.
* Build as usual; the **Mustache overlays** generate thin wrappers:

    * `x-api-wrapper`, `x-api-wrapper-datatype`
    * When present, `x-data-container` + `x-data-item` enable nested generics like `Page<T>`.
* `.openapi-generator-ignore` prevents duplicate DTOs (e.g., `Page`, `Meta`) if provided by the shared `common` package.

---

## Pull Request checklist

Before hitting “Create pull request”:

* [ ] Scope is minimal and focused.
* [ ] Build passes locally: `mvn -q -ntp clean verify`.
* [ ] Tests added/updated where it makes sense (esp. for envelope/generics or error handling).
* [ ] Docs updated if behavior changes (`README.md`, `docs/adoption/*`, or Discussions).
* [ ] No accidental changes to generated code outside intended overlays.
* [ ] Title uses a helpful prefix (e.g., `feature:`, `bugfix:`, `docs:`).

---

## Labels we use

* **`enhancement`** – new capability, refactor that adds value, developer-experience.
* **`bug`** – incorrect behavior, failing test, broken generation edge case.
* **`documentation`** – README / Guides / GitHub Pages / examples.
* **`good first issue`** – small, well-scoped tasks for first-time contributors.
* **`help wanted`** – we’d love community input or PRs here.
* **`discussion needed`** – needs design consensus before implementation.

> Maintainers triage new issues with one or more of the above to guide contributors.

---

## Security

If you discover a **security issue**, **do not** open a public issue.
Email the maintainer at **[baris.sayli@gmail.com](mailto:baris.sayli@gmail.com)** with details. We’ll respond promptly.

---

## License

This project is licensed under **MIT** (see [LICENSE](./LICENSE)).
By contributing, you agree your contributions are licensed under the same terms.
