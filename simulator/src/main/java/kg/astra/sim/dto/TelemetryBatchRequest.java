package kg.astra.sim.dto;

import java.util.List;

/** POST /api/telemetry */
public record TelemetryBatchRequest(List<TelemetryReadingDto> readings) {}
