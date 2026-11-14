package kg.astra.health;

import kg.astra.domain.HealthStatus;

public record AreaStatusDto(HealthArea area, HealthStatus status, String explanation) {}
