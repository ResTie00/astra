# Astra

Onboard health monitor for long-duration missions: spots changes astronauts miss in themselves
and proposes a safer schedule. NASA Space Apps Challenge 2026, Bishkek.

## Run (dev)
```bash
docker compose up -d postgres
cd core && ./mvnw spring-boot:run
```
Swagger UI: http://localhost:8080/swagger-ui.html

## Run (everything)
```bash
docker compose --profile full up --build
```

## Structure
- `core/` Spring Boot backend
- `simulator/` telemetry simulator
- `web/` React UI
- `mocks/` JSON mocks for the UI
- `CONTRACTS.md` module and API contracts

## Open-source tools used
Spring Boot, Hibernate, Flyway, PostgreSQL, springdoc-openapi, Google OR-Tools, Lombok,
React, Vite, TypeScript, TanStack Query, Zustand, Tailwind CSS, shadcn/ui, ECharts.
