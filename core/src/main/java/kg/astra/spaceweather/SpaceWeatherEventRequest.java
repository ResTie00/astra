package kg.astra.spaceweather;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

/**
 * POST /api/space-weather. From the simulator scenario first, from real NASA data later.
 *
 * @param type              "SPE" (solar particle event) for now
 * @param peakProtonFluxPfu >=10 MeV channel, pfu
 */
public record SpaceWeatherEventRequest(
        @NotBlank String type,
        @NotNull Instant onset,
        @NotNull Instant peak,
        @Positive double peakProtonFluxPfu,
        @Positive int durationHours) {}
