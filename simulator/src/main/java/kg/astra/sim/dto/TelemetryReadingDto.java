package kg.astra.sim.dto;

import java.time.Instant;

/** Copy of Core's contract (without validation). Keep field names identical to Core. */
public record TelemetryReadingDto(String crewId, Instant timestamp, Metric metric, double value) {}
