# 🧩 Pull Request Template

Thank you for contributing to **openapi-generics**! 🙌
This repository is **contract‑first**, **projection‑driven**, and **generator‑sensitive** — small changes can affect:

* Contract semantics
* OpenAPI projection output
* Generated client behavior
* Long-term determinism guarantees

This template is intentionally strict. It ensures reviewers evaluate **contract impact first**, implementation second.

---

## 🎯 Summary

> **What does this PR change and *why*?**
> One short paragraph max. Focus on **intent and contract impact**, not implementation details.

**Example:**

> Adds safe multi‑language fallbacks for `x-api-wrapper` to prevent duplicated wrapper models in non‑Java generators.

---

## 📦 Changes

List only **essential changes**:

* Contract updates (`ServiceResponse<T>`, paging, error model)
* OpenAPI projection / schema generation changes
* Vendor extensions (`x-*`) behavior
* Generator or template changes
* Build / CI / docs updates

Avoid implementation noise — reviewers will inspect the diff.

---

## 🧠 Outcome / Impact

Describe the **practical impact**:

* Improves contract clarity or correctness
* Improves projection determinism
* Reduces duplicated or unstable generated models
* Improves server–client alignment
* Improves generator behavior across languages
* Improves CI reliability or build determinism

> If this PR changes observable output (OpenAPI schema, generated code, runtime behavior),
> describe it explicitly.

---

## 🔐 Contract Awareness

This project treats **contracts as the single source of truth**.

**Contract impact:** `yes / no`

If **yes**, specify what is affected:

* Contract surface (`ServiceResponse<T>`, `Page<T>`, `Meta`, error model)
* OpenAPI schema output or naming
* Vendor extension semantics
* Generator or template behavior

> 💡 If unsure → mark **yes** and explain.

---

## 🌍 Multi‑Language Considerations (if applicable)

If this PR touches projection or generation:

* Does it affect **non‑Java generators**?
* Are vendor extensions **safe / no‑op** outside Java?
* Does it reduce or introduce duplicated models?

Reference related issues if relevant.

---

## 🧱 Affected Layer (choose all that apply)

* [ ] Contract (`openapi-generics-contract`)
* [ ] Projection (`openapi-generics-server-starter`)
* [ ] Generator (`openapi-generics-java-codegen`)
* [ ] Generator Parent (`openapi-generics-java-codegen-parent`)
* [ ] Build / CI
* [ ] Documentation

---

## 🔄 Dependency Path Impact (if relevant)

Indicate which flow is affected:

**Server path:**

```
server-starter → contract
```

**Client path:**

```
codegen-parent → codegen → contract
```

---

## ✅ Checklist

* [ ] Scope is minimal and focused
* [ ] Build passes: `mvn -q -ntp clean verify`
* [ ] Tests added/updated if needed
* [ ] Docs updated if behavior changed
* [ ] Contract impact evaluated
* [ ] No direct edits to generated code
* [ ] Changes applied at correct layer (contract / projection / generator)
* [ ] Linked issue or discussion (if applicable)

---

## 🧾 Metadata

* **Type:** feature / bugfix / docs / refactor / chore / test / ci
* **Related Issue:** (optional)
* **Target Release:** (optional)

---

## 💡 Review Tips

* Prefer small, focused PRs
* Never patch generated output
* Fix issues at the correct abstraction layer
* Open a Discussion if contract impact is unclear

---

**Core principle:**

> Contracts are defined at generation time — not corrected downstream.
