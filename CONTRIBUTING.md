# Contributing Guide

Thanks for your interest in improving **openapi-generics**!
This repository provides a **contract-first, generics-aware OpenAPI platform**, centered around a deterministic API lifecycle:

> **Contract → OpenAPI projection → Code generation**

Built with:

* Java 21
* Spring Boot 3.5.x
* OpenAPI Generator 7.x

> Be kind. Be constructive. See our [Code of Conduct](./CODE_OF_CONDUCT.md).

---

## Table of contents

* [Questions & support](#questions--support)
* [How to contribute](#how-to-contribute)
* [Development setup](#development-setup)
* [Project layout](#project-layout)
* [Coding style & commits](#coding-style--commits)
* [Testing & coverage](#testing--coverage)
* [Architecture principles (important)](#architecture-principles-important)
* [Pull Request checklist](#pull-request-checklist)
* [Labels we use](#labels-we-use)
* [Security](#security)
* [License](#license)

---

## Questions & Support

Have a question, design idea, or usage concern?

* Use **GitHub Discussions → Ideas** for:

  * API contract design
  * generics handling (`ServiceResponse<T>`, `Page<T>`)
  * projection behavior

* Use **GitHub Discussions → Q&A** for:

  * setup and configuration
  * integration questions
  * generator usage

- Found a bug?

  * Open an **Issue** with a minimal reproduction

Please search existing issues and discussions first.

---

## How to contribute

1. **Fork** and create a branch:

```bash
git checkout -b feature/scope-short-title
```

2. Keep changes **small and focused**
3. Update tests and docs if behavior changes
4. Run full build
5. Open PR with clear explanation

> Small PRs = faster review

---

## Development setup

### Prerequisites

* Java 21
* Maven 3.9+

### Build (core platform)

```bash
mvn -q -ntp clean verify
```

---

## Project layout

```
/openapi-generics-contract
/openapi-generics-platform-bom
/openapi-generics-server-starter
/openapi-generics-java-codegen
/openapi-generics-java-codegen-parent
```

---

## Coding style & commits

### Rules

* Keep code minimal and deterministic
* Do NOT patch generated output
* Fix issues at:

  * contract
  * projection
  * generator

### Commit prefixes

* feature:
* bugfix:
* docs:
* refactor:
* test:
* ci:

---

## Testing & coverage

```bash
mvn clean verify
```

Notes:

* Core modules focus on correctness, not runtime apps
* Integration validation happens via sample consumers (external)

---

## Architecture principles (important)

This project follows strict boundaries:

### 1. Contract is the source of truth

* `ServiceResponse<T>` defines semantics
* OpenAPI must be a projection (not authority)

### 2. Determinism

Same input → same output

No hidden behavior

### 3. No duplication

* Contract defined once
* No re-definition in OpenAPI or generated code

### 4. Generated code is disposable

Never fix issues in generated files

Fix:

* contract
* projection
* templates

---

## Pull Request checklist

* [ ] Scope is minimal
* [ ] Build passes
* [ ] Tests updated if needed
* [ ] Docs updated if needed
* [ ] No generated code edits

---

## Labels we use

* enhancement
* bug
* documentation
* good first issue
* help wanted

---

## Security

Do NOT open public issues for vulnerabilities

* Use GitHub Security Advisory
* Or email: [baris.sayli@gmail.com](mailto:baris.sayli@gmail.com)

See SECURITY.md

---

## License

MIT License