# Astra: contracts

## Rules for everyone
- Time: UTC, ISO-8601 (`2026-11-14T06:00:00Z`), `Instant` in Java.
- Inside the solver time = integer **slots of 1 hour** from the horizon start. Only Core converts slots <-> dates.
- Dose: **µSv** in all calculations (integer for the solver, double for Monte Carlo). mSv only in API responses for the UI.
- IDs are strings: `c1`, `t07`. Enums are UPPER_CASE strings.
- Only Adilet changes contracts, and announces it in the team chat.

## Packages
| Package | Owner | Rule |
| --- | --- | --- |
| `kg.astra.domain` | shared (Adilet) | plain Java records + interfaces, no Spring/JPA/Jackson |
| `kg.astra.planner` | Mansur | implements `Replanner`; imports only `domain` |
| `kg.astra.risk` | Arsen | implements `DoseModel`, `ScenarioSampler`, `RiskEvaluator`; imports only `domain` |
| `kg.astra.config.DomainConfig` | Adilet | the only place where implementations are plugged in (stub -> real = one line) |
| everything else in `kg.astra` | Adilet | Spring, DB, API |

## Module flow (inside Core, plain method calls)
1. Core builds `MissionState`, including `DoseTable.build(doseModel, forecast, slots)`.
2. `replanner.replan(state)` -> `ReplanResult` with 2-3 `PlanOption` (never throws on "no solution").
3. `scenarioSampler.sample(forecast, runs, seed)` -> called ONCE, same list for every evaluation.
4. `riskEvaluator.evaluate(plan, state, scenarios)` for the current plan and every option.
5. Core maps to `ReplanResponse` (slots -> dates, µSv -> mSv, reason codes -> text).

Conventions: a free slot counts as `INTERNAL`; status `ACT` = no EVA (hard), `WATCH` = penalty (soft).

## HTTP API (Core)
| Method | Path | Body / response | Who calls |
| --- | --- | --- | --- |
| POST | `/api/telemetry` | `TelemetryBatchRequest` | simulator |
| POST | `/api/environment` | `EnvironmentBatchRequest` | simulator |
| POST | `/api/space-weather` | `SpaceWeatherEventRequest` | simulator / NASA data |
| POST | `/api/checkin` | `CheckinRequest` -> `CheckinResponse` | UI, simulator (history) |
| GET | `/api/crew/{id}/status` | `CrewStatusResponse` | UI |
| POST | `/api/replan` | `ReplanStartRequest` -> `ReplanResponse` | UI |
| POST | `/api/replan/{runId}/accept` | `AcceptPlanRequest` -> `AcceptPlanResponse` | UI |
| GET | `/api/stream` | SSE of `StreamEvent` (`type` + `payload`) | UI |

Mocks for the UI: `mocks/*.json`.

**Never send injected-anomaly labels to Core.** They go only to the simulator's CSV export.
