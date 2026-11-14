package kg.astra.sim.dto;

import java.util.List;

/** POST /api/environment */
public record EnvironmentBatchRequest(List<EnvironmentReadingDto> readings) {}
