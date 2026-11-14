package kg.astra.sim.dto;

import java.time.Instant;

public record EnvironmentReadingDto(String module, Instant timestamp, EnvMetric metric, double value) {}
