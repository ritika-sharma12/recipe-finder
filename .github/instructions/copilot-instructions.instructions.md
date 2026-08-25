---
applyTo: '**'
---
This project is a Java 17 Spring Boot 3.1.5 REST API for finding recipes by available ingredients. Use Maven for builds and tests, PostgreSQL for persistence, Spring Web for HTTP APIs, Spring Data JPA/Hibernate for data access, Bean Validation for request validation, and Lombok only where it matches existing conventions.

Follow the existing layered structure under `src/main/java/com/recipefinder`: controllers handle HTTP concerns, services contain business logic, repositories handle persistence, DTOs define API payloads, and model classes represent JPA entities. Keep API contracts and database relationships backward compatible unless a change is explicitly requested. Prefer focused, incremental changes over broad rewrites.

Use clear Java naming, immutable or narrowly scoped data where practical, constructor injection, and explicit validation. Normalize and validate ingredient search input consistently, handle missing resources with appropriate HTTP responses, and avoid exposing persistence entities directly when an existing DTO is appropriate. Do not log credentials, user passwords, or other sensitive data.

For PostgreSQL and JPA changes, preserve referential integrity, constraints, indexes, and naming conventions. Consider query performance and transaction boundaries, and update `schema.sql` or related documentation when the database shape or setup changes. Keep secrets and environment-specific connection values out of source control.

Add or update focused tests for changed behavior using the existing Spring Boot test setup. Run the relevant Maven tests and keep changes compatible with the documented API endpoints and project setup. Update nearby documentation when behavior, configuration, or usage changes.
