package kg.astra.telemetry;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/** POST /api/telemetry. @Valid on the list is required, otherwise items are not validated. */
public record TelemetryBatchRequest(@NotEmpty List<@Valid TelemetryReadingDto> readings) {}
