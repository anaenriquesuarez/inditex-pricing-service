# Pricing Service

REST API developed with Java 17 and Spring Boot 3.5.7 to retrieve the applicable price for a product, brand and application date.

## Overview

The service determines the final price of a product based on a set of price lists with defined validity periods and priorities.

When multiple price lists are applicable at the requested date and time, the service selects the one with the highest priority.

The application uses an in-memory H2 database initialized with the sample data provided in the technical assessment.

## Technology Stack

- Java 17
- Spring Boot 3.5.7
- Spring Web
- Spring Data JPA / Hibernate
- H2 Database
- Maven
- JUnit 5
- Spring MockMvc
- Jakarta Bean Validation
- Lombok

## Architecture

The project follows **Hexagonal Architecture (Ports and Adapters)** to separate business logic from external technologies.

### Domain

Contains the core business model, repository contract and domain-specific exceptions.

- `Price`: Immutable representation of a price record.
- `PriceRepositoryPort`: Defines the contract for retrieving an applicable price.
- `PriceNotFoundException`: Represents the absence of an applicable price.

### Application

Orchestrates the use case without depending directly on the persistence implementation.

- `GetApplicablePriceUseCase`: Input port.
- `GetApplicablePriceService`: Use case implementation.

### Infrastructure

Contains adapters and framework-specific implementations.

- `PriceController`: REST input adapter.
- `PriceResponse`: API response DTO.
- `PriceJpaRepository`: Spring Data JPA repository.
- `PricePersistenceAdapter`: Persistence output adapter.
- `PriceEntity`: JPA entity.
- `GlobalExceptionHandler`: Centralized HTTP exception handling.

## API Endpoint

### GET /api/v1/prices

Retrieves the applicable price for a product and brand at a specified date and time.

**Request parameters**

| Parameter | Type | Required |
|---|---|---|
| applicationDate | LocalDateTime (ISO 8601) | Yes |
| productId | Long | Yes |
| brandId | Long | Yes |

**Example request**

```http
GET /api/v1/prices?applicationDate=2020-06-14T16:00:00&productId=35455&brandId=1
```

**Example response**

```json
{
  "productId": 35455,
  "brandId": 1,
  "priceList": 2,
  "startDate": "2020-06-14T15:00:00",
  "endDate": "2020-06-14T18:30:00",
  "price": 25.45,
  "currency": "EUR"
}
```

## Price Selection Rules

The applicable price is selected according to the following rules:

1. The brand and product identifiers must match.
2. The application date must fall within the price validity period, including both boundaries.
3. If multiple prices are applicable, the record with the highest numerical priority is selected.
4. In case of equal priority, the record with the highest internal ID is selected as a deterministic tie-breaking rule.
5. The API returns a single price result.

Filtering, sorting and result limiting are performed at the persistence layer to avoid unnecessary in-memory processing.

## Database

The application uses an H2 in-memory database.

The schema is generated automatically by Hibernate, and the sample price records are loaded through `data.sql` at startup.

**H2 Console**

```text
http://localhost:8080/h2-console
```

Connection settings:

- JDBC URL: `jdbc:h2:mem:pricingdb`
- Username: `sa`
- Password: empty

The database is recreated when the application restarts.

## Running the Application

**Prerequisites**

- JDK 17
- Internet access for the initial Maven dependency download

**Run using Maven Wrapper**

On macOS/Linux:

```bash
./mvnw spring-boot:run
```

On Windows:

```cmd
mvnw.cmd spring-boot:run
```

The application will start on:

```text
http://localhost:8080
```

## Running the Tests

Execute the complete test suite using:

```bash
./mvnw clean test
```

The integration tests use Spring Boot, MockMvc and the H2 database.

### Test Coverage

The project includes 13 integration tests covering:

- Five price selection scenarios required by the assessment.
- Nonexistent product handling (HTTP 404).
- Invalid application date format (HTTP 400).
- Negative product identifier validation (HTTP 400).
- Zero product identifier validation (HTTP 400).
- Zero brand identifier validation (HTTP 400).
- Missing application date validation (HTTP 400).
- Inclusive price validity start boundary.
- Inclusive price validity end boundary.

## Error Handling

The API uses centralized exception handling.

| Situation | HTTP status | Error code |
|---|---|---|
| No applicable price found | 404 | PRICE_NOT_FOUND |
| Invalid positive identifier constraint | 400 | INVALID_PARAMETERS |
| Missing or malformed request parameter | 400 | Framework-generated validation error |

## Design Decisions

- **Hexagonal architecture:** Business-facing contracts are separated from persistence and HTTP adapters.
- **Dependency inversion:** Application services depend on interfaces rather than database implementations.
- **Immutable domain model:** Java records are used to represent price data.
- **Monetary precision:** `BigDecimal` is used for monetary values.
- **Efficient retrieval:** The query filters and orders prices in the database and limits retrieval to one record.
- **Deterministic selection:** Equal-priority records are resolved using the internal identifier.
- **Automated integration testing:** Endpoint behavior is verified against an initialized H2 database.

## Notes

The in-memory database and H2 console are intended for development and technical assessment purposes.

The tie-breaking rule for equal priorities is an implementation decision, as the original requirements specify only that higher numerical priorities take precedence.