package kg.astra.telemetry;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/** POST /api/environment */
public record EnvironmentBatchRequest(@NotEmpty List<@Valid EnvironmentReadingDto> readings) {}
