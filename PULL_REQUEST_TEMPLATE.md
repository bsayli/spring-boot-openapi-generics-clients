# 🧩 Pull Request Template

Thank you for contributing to **spring-boot-openapi-generics-clients**! 🙌
This repository is **contract‑driven** and **generator‑sensitive** — small changes can affect **server–client alignment**, **generated code**, and **long‑term guarantees**.

This template is intentionally strict. It helps reviewers reason about **contract impact first**, implementation second.

---

## 🎯 Summary

> **What does this PR change and *why*?**
> One short paragraph max. Focus on **intent and contract impact**, not implementation details.

**Example:**

> Adds safe multi‑language fallbacks for `x-api-wrapper` to prevent duplicated wrapper models in non‑Java generators.

---

## 📦 Changes

List the **essential** changes only:

* Added / removed / refactored modules or files
* Updated OpenAPI schema enrichment or vendor extensions
* Modified Mustache templates or generator configuration
* Updated documentation or adoption guides

Avoid implementation noise — reviewers will read the diff.

---

## 🧠 Outcome / Impact

Describe the **practical impact** of this change:

- Improves API contract consistency or clarity
- Reduces duplicated or unstable generated code
- Improves server–client alignment
- Clarifies how generics and paging are handled
- Improves CI reliability, test coverage, or build determinism

> If this PR introduces any observable change  
> (such as generated code shape, OpenAPI schema output, or client behavior),  
> please describe it explicitly so reviewers can assess the impact.

---

## 🔐 Contract Awareness

This project treats **contracts as first‑class artifacts**.

Please evaluate and declare impact:

**Contract impact:** `yes / no`

If **yes**, briefly explain what is affected:

* `api-contract` surface (e.g. `ServiceResponse<T>`, paging, error models)
* OpenAPI schema output or naming
* Vendor extension semantics (`x-api-wrapper`, `x-data-container`, …)
* Generator or Mustache template behavior

> 💡 If you are unsure, default to **yes** and explain. Reviewers will help assess.

---

## 🌍 Multi‑Language Considerations (if applicable)

If this PR touches **vendor extensions**, **templates**, or **generator behavior**, clarify:

* Does this change affect **non‑Java generators** (TypeScript, Kotlin, etc.)?
* Are vendor extensions **safe to ignore** or **no‑op** in other languages?
* Does this reduce or introduce duplicated models in other ecosystems?

**Reference example:**
Issue #7 — *Add Multi‑Language Fallbacks for `x-api-wrapper`*
👉 [https://github.com/bsayli/spring-boot-openapi-generics-clients/issues/7](https://github.com/bsayli/spring-boot-openapi-generics-clients/issues/7)

Link related issues or discussions where relevant.

---

## ✅ Checklist

Please confirm the following:

* [ ] Scope is minimal and focused
* [ ] Build passes locally **from repository root**: `mvn -q -ntp clean verify`
* [ ] Tests added/updated where appropriate
* [ ] Docs updated if behavior or guarantees changed (`README.md`, `docs/`, adoption guides)
* [ ] **Contract impact evaluated** (`api-contract` / OpenAPI / templates)
* [ ] Generated code treated as **disposable output** (changes target contracts, templates, or generators)
* [ ] No accidental edits to generated code outside intended overlays
* [ ] Linked issue or discussion (e.g. `Closes #7`)

---

## 🧾 Metadata

**Type:** `feature` / `bugfix` / `docs` / `refactor` / `chore` / `test` / `ci`
**Related Issue / Discussion:** (optional) `#number`
**Target Release:** (optional) e.g. `v0.7.7`

---

> 💡 **Tips for a smooth review**
>
> * Prefer small PRs with a single architectural intent
> * Avoid touching generated sources unless explicitly required
> * If contract impact is unclear, open a **Discussion** before implementation
>
> **Good PR titles:**
>
> * `feature(client): support Page-only nested generics`
> * `bugfix(server): prevent schema drift on composed responses`
> * `docs(adoption): clarify single-contract semantics`

This template reflects the project’s core principle:
**contracts are defined at generation time — not corrected downstream.**
