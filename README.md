# Purchase Transaction Service

A self-contained Spring Boot REST API that stores purchase transactions in US dollars and converts them to foreign currencies using live exchange rates from the [U.S. Treasury Reporting Rates of Exchange](https://fiscaldata.treasury.gov/datasets/treasury-reporting-rates-exchange/treasury-reporting-rates-of-exchange) API.

No external database, web server, or servlet container installation required — the application runs entirely from the JAR.

---

## Table of Contents

- [Requirements](#requirements)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
- [Running the Tests](#running-the-tests)
- [API Reference](#api-reference)
  - [Store a Purchase Transaction](#1-store-a-purchase-transaction)
  - [Convert a Transaction to a Foreign Currency](#2-convert-a-transaction-to-a-foreign-currency)
- [Field Validation Rules](#field-validation-rules)
- [Currency Conversion Rules](#currency-conversion-rules)
- [Error Responses](#error-responses)
- [Project Structure](#project-structure)
- [H2 Console](#h2-console)

---

## Requirements

| Tool | Version |
|------|---------|
| Java | 17 or higher |
| Maven | 3.8 or higher |

---

## Technology Stack

| Concern | Technology |
|---------|-----------|
| Framework | Spring Boot 3.2 |
| Persistence | Spring Data JPA + H2 in-memory database |
| Validation | Jakarta Bean Validation |
| HTTP Client | Spring RestTemplate |
| Exchange Rates | U.S. Treasury FiscalData API |
| Testing | JUnit 5, AssertJ, Mockito, MockRestServiceServer |

---

## Getting Started

### Clone and run

```bash
git clone <repository-url>
cd wex-inventory-app
mvn spring-boot:run
```

The application starts on **http://localhost:8080**.

### Build a runnable JAR

```bash
mvn clean package
java -jar target/wex-inventory-app-1.0.0.jar
```

---

## Running the Tests

```bash
mvn test
```

The test suite covers:

| Test Class | Scope |
|---|---|
| `PurchaseTransactionControllerTest` | Full-stack controller tests via MockMvc (store + convert endpoints, all validation and error cases) |
| `TreasuryExchangeRateServiceTest` | HTTP integration tests using `MockRestServiceServer` — verifies URL construction, 6-month date window, currency encoding, and error handling |
| `CurrencyConversionServiceTest` | Unit tests using Mockito — verifies conversion logic, rounding behaviour, and exception propagation |

---

## API Reference

### 1. Store a Purchase Transaction

**`POST /transactions`**

Stores a new purchase transaction and assigns it a unique identifier.

#### Request body

```json
{
  "description": "Office supplies",
  "transactionDate": "2024-07-04",
  "purchaseAmount": "49.99"
}
```

| Field | Type | Rules |
|---|---|---|
| `description` | String | Required. Max 50 characters. |
| `transactionDate` | String | Required. Format: `yyyy-MM-dd`. |
| `purchaseAmount` | Decimal | Required. Positive value. Max 2 decimal places. |

#### Response — `201 Created`

```json
{
  "id": "a3f1c2d4-58e7-4b9a-bc12-3e7f6a901234",
  "description": "Office supplies",
  "transactionDate": "2024-07-04",
  "purchaseAmount": 49.99
}
```

#### Example

```bash
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Office supplies",
    "transactionDate": "2024-07-04",
    "purchaseAmount": "49.99"
  }'
```

---

### 2. Convert a Transaction to a Foreign Currency

**`GET /transactions/{id}/convert?targetCurrency={currency}`**

Retrieves a stored transaction and returns its purchase amount converted to the specified currency using the Treasury exchange rate active on or before the transaction date.

#### Path parameter

| Parameter | Description |
|---|---|
| `id` | UUID of the stored transaction |

#### Query parameter

| Parameter | Description |
|---|---|
| `targetCurrency` | Exact currency name from the Treasury API (e.g. `Euro Zone-Euro`) |

#### Response — `200 OK`

```json
{
  "id": "a3f1c2d4-58e7-4b9a-bc12-3e7f6a901234",
  "description": "Office supplies",
  "transactionDate": "2024-07-04",
  "purchaseAmountUsd": 49.99,
  "targetCurrency": "Euro Zone-Euro",
  "exchangeRate": 0.921,
  "exchangeRateDate": "2024-06-30",
  "convertedAmount": 46.04
}
```

| Field | Description |
|---|---|
| `purchaseAmountUsd` | Original amount in US dollars |
| `exchangeRate` | Rate used (foreign currency units per 1 USD) |
| `exchangeRateDate` | Date of the rate record used from the Treasury API |
| `convertedAmount` | Result of `purchaseAmountUsd × exchangeRate`, rounded to 2 decimal places |

#### Example

```bash
curl "http://localhost:8080/transactions/a3f1c2d4-58e7-4b9a-bc12-3e7f6a901234/convert?targetCurrency=Euro%20Zone-Euro"
```

#### Common currency names

| Currency | `targetCurrency` value |
|---|---|
| Euro | `Euro Zone-Euro` |
| British Pound | `United Kingdom-Pound` |
| Japanese Yen | `Japan-Yen` |
| Canadian Dollar | `Canada-Dollar` |
| Mexican Peso | `Mexico-Peso` |
| Australian Dollar | `Australia-Dollar` |
| Swiss Franc | `Switzerland-Franc` |
| Chinese Yuan | `China-Renminbi` |
| Indian Rupee | `India-Rupee` |

To see all supported currencies:

```
GET https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange?fields=country_currency_desc&page[size]=200&sort=country_currency_desc
```

---

## Field Validation Rules

| Rule | Detail |
|---|---|
| Description max length | 50 characters |
| Transaction date format | `yyyy-MM-dd` (e.g. `2024-07-04`) |
| Purchase amount | Must be positive (≥ $0.01) and have at most 2 decimal places |
| Unique identifier | UUID auto-assigned on store; cannot be set by the caller |

---

## Currency Conversion Rules

| Rule | Detail |
|---|---|
| Rate selection | Most recent rate with `record_date ≤ transactionDate` |
| Maximum lookback | 6 months before the transaction date |
| No rate found | Returns `422 Unprocessable Entity` |
| Rounding | Converted amount rounded to 2 decimal places using `HALF_UP` |

---

## Error Responses

All errors return a JSON body in this shape:

```json
{
  "timestamp": "2024-07-04T12:00:00.000",
  "status": 400,
  "error": "Description of the problem",
  "details": ["field-level messages, if any"]
}
```

| Scenario | HTTP Status |
|---|---|
| Validation failure (invalid field values) | `400 Bad Request` |
| Missing required query parameter | `400 Bad Request` |
| Transaction not found | `404 Not Found` |
| No exchange rate within 6 months | `422 Unprocessable Entity` |
| Treasury API unreachable or returns error | `503 Service Unavailable` |

---

## Project Structure

```
src/
├── main/
│   ├── java/com/wex/transaction/
│   │   ├── TransactionApplication.java       # Spring Boot entry point
│   │   ├── config/
│   │   │   └── AppConfig.java                # RestTemplate bean (5s/10s timeouts)
│   │   ├── controller/
│   │   │   └── PurchaseTransactionController.java
│   │   ├── dto/
│   │   │   ├── CreateTransactionRequest.java  # POST request body
│   │   │   ├── PurchaseTransactionResponse.java
│   │   │   ├── ConversionResponse.java        # GET convert response
│   │   │   └── ExchangeRateResult.java        # Internal record
│   │   ├── entity/
│   │   │   └── PurchaseTransaction.java       # JPA entity
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── TransactionNotFoundException.java
│   │   │   ├── CurrencyConversionException.java
│   │   │   └── TreasuryApiException.java
│   │   ├── repository/
│   │   │   └── PurchaseTransactionRepository.java
│   │   └── service/
│   │       ├── PurchaseTransactionService.java
│   │       ├── CurrencyConversionService.java
│   │       └── TreasuryExchangeRateService.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/wex/transaction/
        ├── PurchaseTransactionControllerTest.java
        ├── TreasuryExchangeRateServiceTest.java
        └── CurrencyConversionServiceTest.java
```

---

## H2 Console

An in-memory H2 database is used for persistence. Data is lost when the application stops. During development, the H2 web console is available at:

```
http://localhost:8080/h2-console
```

| Setting | Value |
|---|---|
| JDBC URL | `jdbc:h2:mem:transactiondb` |
| Username | `sa` |
| Password | *(leave blank)* |
