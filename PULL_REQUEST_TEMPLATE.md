# 🧩 Pull Request Template

Thank you for contributing to **spring-boot-openapi-generics-clients**! 🙌
Please review the checklist and provide context to help maintainers review efficiently.

---

## 🎯 Summary

> Short description of what this PR does and *why* — one paragraph max.

Example:

> Implements the cleanup described in issue [#9](https://github.com/bsayli/spring-boot-openapi-generics-clients/issues/9), removing redundant DTOs and refining client generation.

---

## 📦 Changes

List key changes briefly (add/remove/modify):

* Added / removed / refactored modules or files
* Updated docs or configuration
* Improved client generation logic
* Fixed envelope or schema alignment

---

## 🧠 Outcome / Impact

Explain the impact:

* Cleaner or smaller generated output
* Improved readability or maintainability
* Aligned server–client contract
* Enhanced CI or test coverage

---

## ✅ Checklist

* [ ] Scope is minimal and focused
* [ ] Build passes locally: `mvn -q -ntp clean verify`
* [ ] Tests added/updated where appropriate
* [ ] Docs updated (`README.md`, `docs/`, or `adoption guides`)
* [ ] No accidental changes in generated code outside overlays
* [ ] Linked issue (e.g., `Closes #9`)

---

## 🧾 Metadata

**Type:** `feature` / `bugfix` / `docs` / `refactor` / `chore` / `test` / `ci`
**Related Issue:** (optional) `#issue-number`
**Target Release:** (optional) e.g. `v0.7.3`

---

> 💡 *Tip:* Keep PR titles short but meaningful — for example:
>
> * `feature(client): add envelope auto-registration`
> * `bugfix(server): fix schema drift on nested generics`
> * `docs(repo): improve contributor setup guide`
