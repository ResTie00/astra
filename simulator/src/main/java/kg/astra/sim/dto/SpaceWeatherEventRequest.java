package kg.astra.sim.dto;

import java.time.Instant;

/** POST /api/space-weather */
public record SpaceWeatherEventRequest(String type, Instant onset, Instant peak,
                                       double peakProtonFluxPfu, int durationHours) {}
