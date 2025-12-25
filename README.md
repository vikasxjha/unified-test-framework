# 🚀 Unified Automation Framework

A **production-grade, enterprise-scale test automation platform** designed to validate **Web, Mobile, API, Events, Security, Performance, Accessibility, and Chaos** scenarios end-to-end.

This framework is built to support **large organizations**, **CI/CD pipelines**, and **AI-assisted development (Copilot)** with strong architectural guardrails.

---

## 📌 Key Goals

- One framework → **all test types**
- Same tests → **local & CI/CD**
- Strong separation of concerns
- Scalable for **hundreds of engineers**
- Safe usage of **GitHub Copilot**
- Fast feedback via **smoke tests**
- Deep confidence via **E2E & non-functional tests**

---

## 🧰 Tech Stack

| Layer | Technology |
|-----|-----------|
| Language | Java 17 |
| Build | Maven |
| BDD | Cucumber |
| Test Runner | TestNG |
| Web | Playwright |
| Mobile | Appium 2.x |
| API | RestAssured |
| Events | Kafka |
| Contracts | Pact |
| Security | OWASP ZAP |
| Accessibility | axe-core |
| Reporting | Allure |
| CI/CD | GitHub Actions |
| AI | GitHub Copilot (guarded) |

---

## 🏗️ High-Level Architecture


✔ Clean separation  
✔ No business logic in stepdefs  
✔ No assertions in Page Objects

---

## 📁 Project Structure (Simplified)


---

## 🧪 Supported Test Types

| Type | Description |
|----|------------|
| Smoke | Fast confidence checks |
| Web | UI flows using Playwright |
| Mobile | Android & iOS via Appium |
| API | REST validation |
| Events | Kafka event validation |
| Security | Headers, auth, OWASP |
| Accessibility | WCAG compliance |
| Performance | SLA validation |
| Chaos | Resilience testing |
| Contract | Pact consumer/provider |

---

## ⚙️ Prerequisites

| Tool | Version |
|----|--------|
| Java | 17+ |
| Maven | 3.9+ |
| Node.js | 18+ |
| Playwright | Latest |
| Appium | 2.x |
| Docker | Optional |

---

## 🧩 One-Time Setup

```bash
git clone <repo-url>
cd unified-test-framework
mvn clean install -DskipTests
npx playwright install
mvn clean test
mvn test -Dcucumber.filter.tags="@smoke"
mvn test \
  -Dplatform=web \
  -Dbrowser=chromium \
  -Dheadless=false \
  -Dcucumber.filter.tags="@web"
mvn test -Dcucumber.filter.tags="@api"
appium
mvn test -Dcucumber.filter.tags="@mobile"
mvn test -DsuiteXmlFile=src/test/resources/testng.xml
allure serve reports/allure-results
