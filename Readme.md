# Bank Transfer Service

A production-ready Spring Boot REST API that simulates money transfer operations between bank accounts, with commission processing and daily summary generation.

---

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL database (Aiven or any provider)

### 1. Configure Database Credentials

Set environment variables (recommended) or update `application.yml` directly:

**IntelliJ** — Run → Edit Configurations → Environment Variables:
```
DB_USERNAME=avnadmin
DB_PASSWORD=your_aiven_password
```

**Terminal (Windows):**
```cmd
set DB_USERNAME=avnadmin
set DB_PASSWORD=your_aiven_password
```

**Terminal (Mac/Linux):**
```bash
export DB_USERNAME=avnadmin
export DB_PASSWORD=your_aiven_password
```

### 2. Run the Application

```bash
git clone <repo-url>
cd bank-transfer-app
mvn spring-boot:run
```

The app starts on **http://localhost:2026**

### 3. Run Tests

Tests run against an in-memory H2 database — no network or Aiven connection required:

```bash
mvn test
```

---

## 🗄️ Database — Aiven PostgreSQL

This application uses **Aiven** as the managed PostgreSQL provider.

### Connection Configuration (`application.yml`)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://<host>:<port>/defaultdb?sslmode=require
    driver-class-name: org.postgresql.Driver
    username: ${DB_USERNAME:avnadmin}
    password: ${DB_PASSWORD:your_password}
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: update
```

### How to get your Aiven connection details

1. Log in at **aiven.io**
2. Click your PostgreSQL service
3. Go to the **Overview** tab
4. Copy the **Host**, **Port**, **User**, and **Password**
5. The JDBC URL format is:
```
jdbc:postgresql://<host>:<port>/defaultdb?sslmode=require
```

### Viewing the database

**Option 1 — Aiven Query Editor (no install needed)**
- Aiven Console → Your Service → **Query Editor** tab
- Run SQL directly in the browser

**Option 2 — DBeaver (recommended desktop client)**
- Download at dbeaver.io
- New Connection → PostgreSQL
- Fill in host, port (`28238` for Aiven), database (`defaultdb`), username, password
- SSL tab → set SSL mode to **require**

**Option 3 — psql (command line)**
```bash
psql "postgres://avnadmin:<password>@<host>:<port>/defaultdb?sslmode=require"
```

### Useful queries
```sql
-- List all tables
SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';

-- View accounts
SELECT * FROM accounts;

-- View transactions newest first
SELECT * FROM transactions ORDER BY created_at DESC;

-- View daily summaries
SELECT * FROM transaction_summaries ORDER BY summary_date DESC;

-- Check ShedLock table
SELECT * FROM shedlock;
```

---

## Test Database — H2 (in-memory)

Tests use a separate H2 in-memory database defined in `src/test/resources/application-test.yml`. This means:
- Tests never touch the Aiven database
- Tests run offline with no network dependency
- Each test run starts with a clean database (`create-drop`)

The test profile is activated automatically via `@ActiveProfiles("test")` on all test classes.

---

## API Endpoints

### Swagger UI
```
http://localhost:2026/swagger-ui.html
```

## Endpoint Reference

### 1. Process Transfer
```http
POST /api/v1/transfers
Content-Type: application/json

{
  "sourceAccountNumber": "0123456789",
  "destinationAccountNumber": "0987654321",
  "amount": 5000.00,
  "description": "Rent payment"
}
```

**Response (201 Created)**
```json
{
  "success": true,
  "message": "Transfer processed",
  "data": {
    "transactionReference": "TXN20240120010203ABCD1234",
    "amount": 5000.00,
    "transactionFee": 25.00,
    "billedAmount": 5025.00,
    "status": "SUCCESSFUL",
    "statusMessage": "Transfer completed successfully",
    "commissionWorthy": false,
    "createdAt": "2024-01-20T01:02:03"
  }
}
```

**Possible statuses:** `SUCCESSFUL`, `INSUFFICIENT_FUND`, `FAILED`

---

### 2. List Transactions (with filters)
```http
GET /api/v1/transfers?status=SUCCESSFUL&accountNumber=0123456789&startDate=2024-01-01&endDate=2024-01-31&page=0&size=20
```

All query parameters are optional. Filters combine with AND logic.

---

### 3. Get Daily Summary
```http
GET /api/v1/summaries/daily?date=2024-01-20
```

Omit `date` to get today's summary. Date cannot be in the future.

---

### 4. Manually Trigger Summary Generation
```http
POST /api/v1/summaries/generate?date=2024-01-19
```

---

## Scheduled Jobs

| Job | Default Schedule | ShedLock Name |
|---|---|---|
| Commission Processing | **1:00 AM daily** | `commissionProcessingJob` |
| Daily Summary Generation | **1:30 AM daily** | `dailySummaryJob` |

```

### Commission Calculation
- **Transaction Fee** = 0.5% of amount, capped at ₦100
- **Commission** = 20% of transaction fee
- A transaction is `commissionWorthy = true` if commission > 0

### ShedLock (Kubernetes Safety)
ShedLock uses the PostgreSQL datasource to ensure scheduled jobs run on **exactly one pod** in a multi-instance deployment. The `shedlock` table is auto-created on startup.

---

## Seeded Test Accounts

These accounts are auto-created on first startup via `DataInitializer`:

| Account Number | Name | Balance |
|---|---|---|
| `0123456789` | Alice Johnson | ₦500,000 |
| `0987654321` | Bob Smith | ₦250,000 |
| `1122334455` | Carol White | ₦1,000,000 |
| `5566778899` | David Brown | ₦50,000 |
| `9988776655` | Eve Davis | ₦5,000 |

---