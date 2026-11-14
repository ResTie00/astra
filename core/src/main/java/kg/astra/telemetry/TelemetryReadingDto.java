package kg.astra.telemetry;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record TelemetryReadingDto(
        @NotBlank String crewId,
        @NotNull Instant timestamp,
        @NotNull Metric metric,
        double value) {}
