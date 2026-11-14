# ARGUS
### Astronaut Readiness & Guardian Unified System

**An onboard health assistant that shows astronauts what they don't notice in themselves, and replans the mission to keep them safe.**

NASA Space Apps Challenge 2026, Bishkek · Challenge: *Create Health Monitoring Software for Astronauts on Space Missions*

> In Greek myth, Argus Panoptes was a guardian with a hundred eyes who never fully slept.
> ARGUS watches over a crew that often does not sleep enough.

<!-- TODO: hero screenshot of the astronaut dashboard -->
![ARGUS dashboard](docs/screenshots/dashboard.png)

---

## The problem

On a mission to Mars, a message to Earth takes up to 20 minutes one way, so astronauts carry much of the responsibility for spotting changes in their own health. But people are poor judges of their own decline: with chronic sleep loss, performance drops while the feeling of being "fine" stays the same. NASA lists both sides of this among the risks of human spaceflight: *Risk of Inadequate Sleep and Irregular Schedules* and *Risk of Injury and Compromised Performance from Spacewalks*.

## What ARGUS does

- **Personal baseline.** Every metric is compared with *this* astronaut's own norm, not a population average.
- **Blind spot detection.** ARGUS compares how astronauts rate themselves with what their data shows (sleep, reaction time, heart rate variability) and flags the gap.
- **Six health areas** with a clear status (normal / watch / act), an explanation and one recommended action: radiation, behavioral health, cardiovascular, immune, bone and muscle, habitat environment.
- **Autonomous replanning.** When someone is impaired or a solar storm is coming, a constraint solver (Google OR-Tools CP-SAT) proposes 2-3 new schedules that respect every safety rule.
- **Risk you can compare.** A Monte Carlo simulation over 2,000 storm scenarios scores the current plan and every option on the same scenarios. The astronaut picks a plan with one tap; the last word is always human.

## Demo story

1. Maya Chen (flight engineer) has slept about 1.4 h less than her norm for three nights. Her reaction time is 11% slower and her HRV 23% below her norm. **She rates herself 4 out of 5. Her data says 1.0.** ARGUS raises a blind spot alert.
2. A solar particle event is forecast. Maya's antenna repair EVA falls inside the storm window.
3. ARGUS replans. The "Balanced" option gives the EVA to a rested crew member outside the storm and moves a second EVA after the storm peak: 2 changes in total, each with a reason.
4. **Risk score 20 → 0. Hours of EVA by an impaired astronaut 4 → 0.** Maya accepts the plan; the ground learns about it with the communication delay.

<!-- TODO: screenshots of the blind spot panel and the plan comparison -->
![Blind spot](docs/screenshots/blind-spot.png)
![Replanning](docs/screenshots/replan.png)

## NASA hazards and risks addressed

| NASA hazard | Official NASA HRP risk | What ARGUS does |
|---|---|---|
| Space Radiation | Risk of Radiation-Induced Cancers | Cumulative dose vs. mission budget, storm forecast, EVAs moved out of the storm window |
| Isolation and Confinement | Risk of Inadequate Sleep and Irregular Schedules; Risk of Behavioral Changes and Psychiatric Disorders | Blind spot: self-rating vs. sleep, reaction time and HRV against the personal baseline |
| Distance from Earth | (hazard) | Decisions made on board; the ground view receives events with a delay |
| Gravity Fields | Risk of Cardiovascular Adaptations; Risk of Spaceflight-Induced Bone Changes | Heart rate and HRV against the personal norm; bone and muscle shown as an indicator |
| Hostile/Closed Environments | Risk of Hypoxia; Risk of Altered Immune System Responses | CO2 per habitat module, body temperature, reported symptoms |
| Several at once | Risk of Injury and Compromised Performance from Spacewalks | A fatigued astronaut is taken off the EVA, and the risk is shown before and after |

Sources: [NASA HRP: 5 Hazards of Human Spaceflight](https://www.nasa.gov/hrp/hazards/), [NASA HRP: Risks of Human Spaceflight](https://www.nasa.gov/hrp/risks/), [CSA: Monitoring Radiation for Astronaut Health](https://www.asc-csa.gc.ca/eng/news/articles/2026/2026-02-26-monitoring-radiation-astronaut-health.asp).

## How it works

```
Telemetry simulator ──HTTP──▶ Core (Spring Boot) ──REST + SSE──▶ Web UI (React)
                               │
                               ├─ personal baseline · blind spot · six health areas · alerts
                               └─ replanning
                                    MissionState ─▶ CP-SAT planner ─▶ 2-3 options
                                                  ─▶ Monte Carlo (same 2,000 scenarios for all)
                                                  ─▶ comparison ─▶ astronaut accepts
```

- **Personal baseline:** robust median and MAD over the last two weeks, *excluding* the last three days, so a slow decline cannot hide inside its own norm.
- **Blind spot:** three signals turned into an objective 1-5 score and compared with the self-rating. Silence is preferred to a false alarm.
- **Planner:** CP-SAT model with hard rules (one executor per task, no overlaps, task windows, skills, no EVA during a storm, no EVA for an astronaut in "act" status, dose budget) and three objectives (balanced, lowest dose, fewest changes). Every change carries a machine-readable reason that is turned into plain language.
- **Risk:** Monte Carlo over sampled storms with a fixed seed; all plans are evaluated on the same scenarios, so the difference between them is real, not sampling noise.

Details: [docs/how-it-works.md](docs/how-it-works.md).

## Honest limits

- ARGUS is a **decision-support prototype, not a medical device**.
- All crew data comes from our **telemetry simulator**: there is no open labelled dataset of astronaut physiology. The simulator gives each crew member a personal norm and injects a "quiet decline" so the detection can be checked against known ground truth.
- Dose rates, the dose budget and alert thresholds are **placeholders** until sourced values are added to [docs/sources.md](docs/sources.md). The dose model tracks total dose only; per CSA, neutrons contribute about 30% of radiation on the ISS and are not modelled separately.
- Bone and muscle is an indicator only: exercise tracking is not connected in this prototype.

## Run it

Requirements: Docker, Java 21, Maven.

```bash
docker compose up -d postgres
cd core && ./mvnw spring-boot:run          # API on http://localhost:8080
cd simulator && mvn -q compile exec:java   # 14 days of history, then live telemetry
```

Demo flow: `curl -X POST localhost:8080/api/demo/reset`, then type `storm` in the simulator console, then replan from the UI.

Everything in containers: `docker compose --profile full up --build` (the simulator still runs separately).

## API

Swagger UI: `http://localhost:8080/swagger-ui.html` · spec snapshot: [docs/openapi.json](docs/openapi.json) (generated, do not edit by hand).

| Endpoint | Purpose |
|---|---|
| `GET /api/crew`, `GET /api/crew/{id}/status` | Crew overview and one astronaut's six areas, blind spot and action |
| `POST /api/checkin` | Self-rating and reaction test |
| `POST /api/replan`, `POST /api/replan/{runId}/accept` | Propose options, accept one |
| `GET /api/schedule`, `GET /api/space-weather/current`, `GET /api/alerts` | Current plan, storm, alerts |
| `GET /api/stream` | Live updates (Server-Sent Events) |
| `POST /api/telemetry`, `/api/environment`, `/api/space-weather` | Data intake (simulator) |
| `GET /api/crew/{id}/trends` | Daily sleep, reaction time, HRV and self-rating with personal norm bands |
## Built with (open source)

**Backend:** Java 21, Spring Boot, Spring Data JPA / Hibernate, Flyway, PostgreSQL, springdoc-openapi, Google OR-Tools (CP-SAT), Jackson, Lombok.
**Simulator:** Java 21, Jackson.
**Frontend:** React, Typescript, Vite, Axios, Zustand, TanStack Query, Lucide React, ECharts.
**Infrastructure:** Docker, Docker Compose.

## Team

| | Role |
|---|---|
| Adilet | Backend, health analytics (baseline, blind spot, status), integration |
| Mansur | CP-SAT mission replanner |
| Bekzat | Frontend |
| Emir | Design, pitch, research and sources |
| Arsen | Team member: simulation and risk modelling design |

## Use of AI

We used AI assistants during development: for parts of the code (including the telemetry simulator and several backend services), for code review, debugging and this README. The architecture, the health logic (personal baseline, blind spot, area rules) and the planning model were designed by the team; all code was reviewed and tested by us, and each of us can explain our part of it.
*Source code codename: `astra` (package `kg.astra`).*
