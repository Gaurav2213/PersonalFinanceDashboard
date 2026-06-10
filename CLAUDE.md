# CLAUDE.md — Personal Finance Dashboard

This file provides full guidance to Claude Code when working in this repository.  
It combines **codebase analysis** (auto-scanned) with **project history and intent** (migrated from ChatGPT context).

---

## Build & Run

```bash
# Compile
mvn compile

# Run the server (main class: controller.ServerApp)
mvn exec:java -Dexec.mainClass="controller.ServerApp"

# Package
mvn package
```

The server starts on **port 8085**. The frontend config (`frontend/js/config.js`) points to `http://localhost:8085`.

---

## Environment Variables

| Variable | Purpose |
|-|-|
| `JWT_SECRET_B64` | Base64-encoded secret (>=32 bytes) — preferred |
| `JWT_SECRET` | Raw string secret (>=32 chars) — fallback |
| `SENDGRID_API_KEY` | SendGrid email delivery |
| `SPLUNK_HEC_URL` | Splunk HTTP Event Collector URL |
| `SPLUNK_HEC_TOKEN` | Splunk HEC auth token |
| `SPLUNK_INDEX` | Splunk index name |
| `SPLUNK_HEC_INSECURE` | Set `true` to disable TLS cert validation |

If no JWT secret env vars are set, a hardcoded dev fallback is used (dev only).

---

## Database

MySQL on `localhost:3306`, database `finance_db`. Credentials in `src/util/DBConnection.java` (hardcoded: `root`/`javaee`). Every DAO method opens its own connection via `DBConnection.getConnection()` — no connection pool.

### Tables (Phase 1 — Current)

- `users` — id, fullName, email, password (bcrypt hashed), isVerified, verificationToken, resetToken, resetTokenExpires
- `transactions` — id, userId, amount, category, type (INCOME/EXPENSE), description, date
- `budgets` — id, userId, category, amount, month

### Tables (Phase 2 — Planned)

```sql
CREATE TABLE categories (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  type ENUM('income', 'expense') NOT NULL,
  user_id INT NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id),
  UNIQUE KEY (name, user_id)
);

CREATE TABLE budget_limits (
  id INT AUTO_INCREMENT PRIMARY KEY,
  category_id INT NOT NULL,
  amount DECIMAL(10, 2) NOT NULL,
  month INT NOT NULL,
  year INT NOT NULL,
  user_id INT NOT NULL,
  FOREIGN KEY (category_id) REFERENCES categories(id),
  FOREIGN KEY (user_id) REFERENCES users(id)
);
```

Do NOT create these tables yet — they are Phase 2. Ask before touching schema.

---

## Architecture

Plain Java HTTP server — no servlet container, no Spring. `com.sun.net.httpserver.HttpServer` handles all HTTP.  
**This is intentional.** The project was built with Core Java + JDBC to demonstrate deep understanding of fundamentals. Do NOT suggest Spring Boot for current implementation.

### Layer Structure

```
controller/        → HttpHandlers (one class per endpoint)
  analytics/       → Read-only analytics endpoints
  batch/           → Bulk transaction operations
  budget/          → Budget CRUD + batch
  auth/            → Auth-specific handlers
  common/          → Shared helpers (valid categories)
service/           → Business logic (UserService, TransactionService, BudgetService, AnalyticsService)
dao/               → SQL queries (UserDAO, TransactionDAO, BudgetDAO)
model/             → POJOs / DTOs
util/              → Cross-cutting concerns (Utils, JWTUtils, EmailService, CategoryValidator)

frontend/          → Static HTML/CSS/JS (no build step)
  auth/            → login.html, register.html
  js/              → auth.js, utils.js, config.js
```

### Request Lifecycle

1. `ServerApp` registers every route via `Guarded.open()` (public) or `Guarded.protect()` (JWT-required).
2. `Guarded` wraps every handler with: CORS headers → MDC tracing → optional JWT auth → business logic → latency logging.
3. `ExchangeWrapper` intercepts `sendResponseHeaders` so the status code is captured in MDC before logging.
4. Authenticated routes receive a `Claims` object; the user's integer ID is available via `exchange.getAttribute("authUserId")`.

---

## Auth Flow

- **Registration:** email + password → `UserService.register()` → bcrypt hash → store with unverified flag → send verification email via SendGrid.
- **Login:** verify password (bcrypt) → check `is_verified` → issue JWT (30 min expiry, includes `sub`=userId and `email` claim, unique `jti`).
- **Logout:** blacklist the token's `jti` in `SessionManager` (in-memory `ConcurrentHashMap`; does not survive restarts).
- **Refresh:** `/auth/refresh` reissues a new JWT from a still-valid token.
- **Password reset:** SHA-256 hash of raw token stored in DB; raw token emailed; 20-min expiry.

### Token / Session Notes

- `SessionManager` blacklist is **in-memory only** — logged-out tokens become valid again after server restart.
- JWT tokens expire after 30 minutes.
- Email verification tokens expire after 24 hours; password reset tokens after 20 minutes.
- Session refresh threshold: 5 minutes before expiry (`REFRESH_THRESHOLD_MILLIS`).

---

## UserService — Two Login Methods

`UserService` has **two distinct login methods** — do not merge them or create a third:

| Method | Used By | Returns | Purpose |
|---|---|---|---|
| `loginWithValidation(email, password)` | API controller layer (LoginHandler) | `AuthResponse<LoginResponse>` | Full validation + JWT token in structured response |

`ValidationResult` is used **internally** by private helpers (`validateEmail`, `validatePassword`) inside `loginWithValidation()` — never surfaces to the controller layer.

**Rule:** Controllers always call `loginWithValidation()`. Never bypass it or call internal helpers directly.

---

## Completed Features ✅

### Authentication & User Management

- `UserDAO` — register, find by email, find by ID, update reset token
- `UserService` — registration with email verification, login with JWT, resend verification
- `RegisterHandler` — POST `/auth/register`
- `LoginHandler` — POST `/auth/login` — returns `AuthResponse<T>` with JWT token
- `LogoutHandler` — POST `/auth/logout` — jti blacklist-based session invalidation
- `ForgotPasswordHandler` — POST `/auth/forgot-password` — sends reset link via SendGrid
- `ResetPasswordHandler` — POST `/auth/reset-password` — validates token + updates password
- `/auth/refresh` — reissues JWT from a still-valid token
- `AuthGuard` / `Guarded` — middleware wrapping all protected routes
- `SessionManager` — in-memory jti blacklist

### Transaction Management

- `TransactionDAO` — add, get all by user, get by category, batch add, batch update, batch delete
- `TransactionService` — business logic + validation
- `AddTransactionHandler` — POST `/transactions/add`
- `GetAllTransactionsHandler` — GET `/transactions/all?userId=`
- `GetTransactionsByCategoryHandler` — GET `/transactions/category?category=`
- `UpdateTransactionHandler` — PUT `/transactions/update` ✅
- `DeleteTransactionHandler` — DELETE `/transactions/delete` ✅
- Batch transaction handlers (add, update, delete) in `controller/batch/`
- `CategoryValidator` — shared validator used across transaction and budget modules

### Budget Management

- `BudgetDAO` — add, update, delete, get by user, check exists
- `BudgetService` — full CRUD with validation
- `BudgetResponse<T>` — generic typed response wrapper
- `AddBudgetHandler` — POST `/budget/add`
- `UpdateBudgetHandler` — PUT `/budget/update`
- `GetBudgetHandler` — GET `/budget?userId=`
- `DeleteBudgetHandler` — DELETE `/budget/delete`
- Batch budget handlers (add, update, delete) in `controller/budget/`

### Analytics (Batch Processing)

All in `AnalyticsService`, exposed via `controller/analytics/`. DSA concepts used listed per feature:

| Feature | DSA Used |
|---|---|
| Top Spending Categories | HashMap + PriorityQueue / custom sort |
| Monthly Spending Summary | TreeMap<YearMonth, Double> |
| Prefix Sum (cumulative daily spend) | Prefix Sum, Array, Map<LocalDate, Double> |
| Budget Utilization % | Map<Category, Double> + arithmetic |
| Max/Min Spending Days | Max Heap / linear scan |
| Recurring Transaction Detector | HashMap<Category, List<LocalDate>>, pattern matching |
| Overspend Warnings | Greedy, filtering, Map<Category, Double> |
| Amount/Keyword Filters | Binary search, Stream filter, custom comparator |
| Spending Distribution by Type | Sum + ratio calculation |
| Dashboard Summary | Aggregate summary for dashboard landing |

### Splunk Integration ✅

- Logback with JSON layout ships to Splunk HEC asynchronously (`ASYNC_SPLUNK` appender, batch of 50 / 2s)
- MDC fields per request: `traceId`, `path`, `method`, `action`, `status`, `latencyMs`, `userId`, `ip`, `ua`
- Two events per request: `request_received` and `request_completed`
- Source type: `_json`
- Config: `src/main/resources/logback.xml`

### Frontend (Partially Integrated)

- Static HTML/CSS/JS in `frontend/` — no build step
- Register page — client-side validation + backend integrated ✅
  - Post-registration: form hides, `.auth-verify-panel` shown with 5s countdown → redirects to login ✅
- Login page — JWT received, stored in `localStorage` ✅
- Shared `auth.css` across login/register — includes `.auth-verify-panel` styles
- Chart.js integrated for spending visualizations
- Budget, Analytics, Dashboard pages — partially implemented, integration in progress

---

## In Progress 🔄

- **Frontend flaws — pending fix:**
  - `maxlength="15"` on password fields in `login.html` and `register.html` — should be `128`
  - No "Forgot Password" link on `login.html` — backend endpoint exists
  - No submit button disable during fetch — double-submit risk on both forms
  - `else{` missing space at `auth.js` line ~226 — style inconsistency
  - `pattern` vs `maxlength` mismatch on `fullName` in `register.html` (pattern allows 60, maxlength caps at 30)
- **Frontend-Backend integration** — registration and login done; logout, refresh, Budget, Analytics, Dashboard pages need wiring
- **Forgot Password frontend** — backend done, frontend form needs connecting
- **Session token refresh on frontend** — backend `/auth/refresh` exists, frontend needs to call it before expiry using JWT `exp` claim (`JSON.parse(atob(token.split('.')[1])).exp * 1000`)

---

## Planned Features 📋

### Kafka (Next Priority)

- Purpose: alerts and notification services
- Use cases: budget overspend alerts, large transaction notifications
- Plan: producer in `TransactionService`/`BudgetService`, consumer sends notifications
- Architecture stays monolithic — Kafka is an internal addition, not a microservice split

### UiPath RPA

- Purpose: email/PDF data extraction
- Use case: parse bank statements (PDF) → auto-import transactions into the system

### HTTPS

- Switch from HTTP to HTTPS (self-signed or Let's Encrypt)
- Steps: generate keystore → configure `HttpsServer` → update `frontend/js/config.js` base URL

### Future (Post-graduation)

- Migrate to Spring Boot for microservices
- Add OAuth2 / Spring Security
- Containerize with Docker + deploy on AWS or Azure

---

## Key Architecture Decisions

1. **Monolithic by design** — microservices deferred to future Spring Boot version.
2. **No Spring** — intentionally uses raw Java SE + JDBC to demonstrate fundamentals.
3. **`Guarded` middleware** — all protected routes go through `Guarded.protect()` which handles CORS, MDC, JWT validation, and latency logging in one place.
4. **userId always from JWT attribute** — never trust userId from query params on protected endpoints. Always use: `int userId = (int) exchange.getAttribute("authUserId");`
5. **JSON body over query params** — POST/PUT endpoints receive JSON body; GET endpoints use query params.
6. **`ValidationResult` pattern** — service methods return `ValidationResult`; controllers check it and send the appropriate JSON response. Never put validation logic in controllers or DAOs.
7. **Phased development** — basic clean version first, then extend. Always ask before adding complexity.
8. **Structured responses always** — never return plain text (except 405). Always use `AuthResponse<T>`, `BudgetResponse<T>`, or `AnalyticsResponse<T>` wrappers.
9. **Two login methods in UserService** — `login()` for internal use, `loginWithValidation()` for API layer. Never merge them.
10. **Phase 2 schema is planned but not built** — `categories` and `budget_limits` tables exist in the plan only. Do not create them without explicit instruction.

---

## Validation Pattern

`ValidationResult` is the centralized validation response class used across all services:

```java
// In service — return ValidationResult:
ValidationResult result = validateInput(...);
if (!result.isSuccess()) return result;

// In controller — check and respond:
ValidationResult result = transactionService.addTransaction(...);
if (!result.isSuccess()) {
    Utils.sendJsonResponse(exchange, 400, result);
    return;
}
```

**Rule:** All validation logic lives in the Service layer. Controllers stay thin — parse input, call service, send response.

### Two Separate Validation Methods in TransactionService

Do NOT merge these — they serve different purposes:

| Method | Validates | Used In |
|---|---|---|
| `validateTransaction(tx)` | amount, type, date, category, description | `addTransaction()` and `updateTransaction()` |
| `validateTransactionIdAndUserId(id, userId)` | id > 0, userId > 0, record exists in DB | `updateTransaction()` and `deleteTransaction()` only |

`validateTransaction()` answers: "Is this a well-formed transaction?"  
`validateTransactionIdAndUserId()` answers: "Does this transaction exist and belong to this user?"

---

## Input Normalization Rule

Always normalize user input **before** validation and **before** calling setters on model objects:

```java
// Always trim and lowercase type/category before validation
String type = transaction.getType().trim().toLowerCase();
String category = transaction.getCategory().trim().toLowerCase();

// Then reassign back to the object so all future getters return clean values
transaction.setType(type);
transaction.setCategory(category);

// Now validate — VALID_TYPES.contains(type) will work correctly
```

**Why:** Without calling `setType()` / `setCategory()` after normalization, the object still holds dirty input (e.g., `" Income "` with spaces), which causes validation failures and inconsistent DB inserts.

---

## Batch Operations — DAO Pattern

All batch DAO methods must follow this exact JDBC pattern for atomicity:

```java
conn.setAutoCommit(false); // group all ops into one transaction
try {
    for (Transaction tx : transactions) {
        stmt.addBatch(); // queue each SQL op
    }
    stmt.executeBatch(); // send all to DB at once
    conn.commit();       // save permanently only if all succeed
} catch (SQLException e) {
    conn.rollback();     // cancel everything on any failure
}
```

| Step | Purpose |
|---|---|
| `setAutoCommit(false)` | Turns off auto-save so nothing commits until you say so |
| `addBatch()` | Queues each SQL statement |
| `executeBatch()` | Runs all queued statements in one round trip |
| `commit()` | Permanently saves all changes |
| `rollback()` | Cancels all changes on failure — atomic |

**Batch size limit:** Always validate that the incoming list is not too large before processing. Reject oversized batches immediately (fail fast) — do not load them into memory or hit the DB.

### Duplicate Check in Batch

For batch add operations, use `isDuplicateTransaction(tx)` inside the validation loop:

| Compare With | Reason |
|---|---|
| Other transactions in DB (excluding current transaction) | ✅ Check for duplicates |
| Same transaction in DB (by ID) | ❌ Skip — it's being updated, not inserted |

Reject the entire batch on the first duplicate found — do not partially commit.

---

## Query Parameter Decoding

All GET endpoints that read query params must use `URLDecoder.decode()`:

```java
String category = URLDecoder.decode(params.get("category"), StandardCharsets.UTF_8);
```

**Why:** Browsers and HTTP clients percent-encode special characters (spaces → `%20`, `&` → `%26`, `/` → `%2F`). Without decoding, category values like `"food & drinks"` arrive as `"food%20%26%20drinks"` and will fail DB lookups.

---

## Response Conventions

All API responses follow one of these shapes:

```json
{ "success": true, "message": "...", "data": { ... } }
{ "success": false, "message": "..." }
```

- `Utils.sendResponse()` — sends raw JSON strings
- `Utils.sendJsonResponse()` — serializes objects via Jackson

### AnalyticsResponse\<T\> — Generic Design

`AnalyticsResponse<T>` uses `T data` (not `List<T> data`). This supports any return type:

```java
AnalyticsResponse<List<CategoryTotal>>   // list results
AnalyticsResponse<Map<String, Double>>   // map results
AnalyticsResponse<BudgetUtilization>     // single object
AnalyticsResponse<String>               // plain message
```

Do NOT change `T data` back to `List<T> data`. The generic design is intentional and already in production across all analytics endpoints.

---

## Logging

Logback + Splunk HEC with MDC fields. Every request logs `request_received` and `request_completed` events. When adding new significant operations, always include relevant MDC fields (`userId`, `traceId`, `action`).

---

## Adding a New Endpoint

1. Create a handler class in the appropriate `controller/` sub-package.
2. Register it in `ServerApp.main()` using `Guarded.open()` or `Guarded.protect()`.
3. Add its path → action string mapping in `Guarded.actionFromPath()`.
4. Add business logic to the relevant `service/` class.
5. Add DB queries to the relevant `dao/` class.
6. Follow `ValidationResult` pattern in the service.
7. Normalize input (trim + toLowerCase) before validation.
8. Decode query params with `URLDecoder.decode()` in GET handlers.
9. Return structured JSON using response wrapper classes.

---

## Rules — Claude Must Always Follow

- **Never suggest Spring** for current implementation
- **Never put business logic in controllers or DAOs** — always in Service layer
- **userId from JWT attribute only** on protected routes — never from query params
- **Follow existing package structure** — don't create new packages without asking
- **Maven project** — all dependencies in `pom.xml`
- **Frontend is vanilla JS** — no React, no framework
- **Phased approach** — implement basic version first, confirm before adding advanced logic
- **Always use `ValidationResult`** — don't invent new validation patterns
- **Always use response wrapper classes** — `AuthResponse<T>`, `BudgetResponse<T>`, `AnalyticsResponse<T>`
- **Always normalize input** — trim() + toLowerCase() then reassign via setter before validating
- **Always decode query params** — use `URLDecoder.decode()` on all GET query param values
- **Never merge the two login methods** — `login()` and `loginWithValidation()` serve different layers
- **Never touch Phase 2 schema** — `categories` and `budget_limits` tables are planned, not built yet
- **Batch ops must be atomic** — always use setAutoCommit/executeBatch/commit/rollback pattern
- **Batch size must be capped** — reject oversized lists before any DB or memory operations
- **`AnalyticsResponse<T>` uses generic T** — never revert to `List<T>`
