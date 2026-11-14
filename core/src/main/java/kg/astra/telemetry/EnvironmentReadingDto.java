package kg.astra.telemetry;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record EnvironmentReadingDto(
        @NotBlank String module,
        @NotNull Instant timestamp,
        @NotNull EnvMetric metric,
        double value) {}
