# 🤖 COPILOT.md
## Unified Automation Framework – Copilot Usage Guide

This document explains **how GitHub Copilot must be used** in this repository.

It complements:
- `copilot-instructions.md` → **hard rules**
- `COPILOT_PROMPTS.md` → **ready-made prompts**
- CI guardrails (`copilot-workflow.yml`, `copilot-chat.yml`)

Together, these ensure Copilot accelerates development **without breaking architecture or quality**.

---

## 🎯 Purpose

Copilot is allowed in this repository **only as an accelerator**, not a replacement for engineering judgment.

This framework is treated as a **company-wide automation platform**, so AI-generated code must meet **Staff-level quality standards**.

---

## 🧭 How Copilot Should Think

Copilot must behave as:

> **A Staff / Principal SDET with strong backend, frontend, and infra understanding**

Copilot must:
- Understand end-to-end flows
- Respect separation of concerns
- Generate code that actually runs
- Prefer clarity over cleverness

Copilot must NOT:
- Guess architecture
- Add shortcuts
- Produce demo-quality code

---

## 🏗️ Repository Mental Model (MANDATORY)

Copilot must understand this mental model before writing code:


Violating this flow is **not allowed**.

---

## 📦 What Copilot Is Allowed to Do

Copilot MAY:

- Generate new classes where required
- Implement missing methods
- Fix compilation issues
- Extend existing APIs
- Refactor duplicated logic
- Improve naming and readability
- Add logging and assertions (in correct layers)

---

## 🚫 What Copilot Is NOT Allowed to Do

Copilot MUST NOT:

- Add `Thread.sleep`
- Add TODO / FIXME comments
- Add assertions inside Page Objects
- Add API logic inside UI classes
- Add hard-coded URLs or credentials
- Introduce new dependencies
- Downgrade existing libraries
- Use Selenium instead of Playwright
- Use `MobileBy` instead of `AppiumBy`

These rules are **enforced in CI**.

---

## ⚙️ Technology Rules (STRICT)

| Area | Required |
|----|----|
| Java | 17 |
| Web | Playwright |
| Mobile | Appium 2.x |
| API | RestAssured |
| BDD | Cucumber |
| Runner | TestNG |
| Build | Maven |
| Reports | Allure |

Any deviation is a **hard violation**.

---

## 🔐 Configuration Rules

Copilot must respect:

- `env-config.json` → environment URLs & flags
- `testdata.json` → sample test data
- System properties → runtime overrides
- Environment variables → secrets

❌ No hard-coded values in code.

---

## 🧪 Testing Philosophy

Copilot-generated tests must be:

- Deterministic
- Fast
- Stable
- CI-safe
- Environment-agnostic

Smoke tests must:
- Run in < 5 minutes
- Never depend on flaky data
- Fail only on real regressions

---

## 🧠 How to Use Copilot Effectively (Human Guidance)

When using Copilot:

✅ Always paste **full context** (feature + related classes)  
✅ Always demand **complete files**  
✅ Always ask for **exact paths**  
✅ Always insist on **compilable code**

❌ Never accept half implementations  
❌ Never accept pseudo-code  
❌ Never accept “example only” answers

---

## 🧪 Required Validation After Copilot Changes

After Copilot generates or modifies code, the following MUST pass:

```bash
mvn clean test -Dcucumber.filter.tags="@smoke"
