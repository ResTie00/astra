package kg.astra.checkin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;
import java.util.List;

/**
 * POST /api/checkin. Used by the UI (live) and by the simulator (history for past days).
 *
 * @param timestamp      null = server time (UI); simulator sends explicit past timestamps
 * @param reactionMeanMs null if the reaction test was skipped
 */
public record CheckinRequest(
        @NotBlank String crewId,
        Instant timestamp,
        @Min(1) @Max(5) int wellbeing,
        @Min(1) @Max(5) int mood,
        @Min(1) @Max(5) int stress,
        List<String> symptoms,
        @Positive Integer reactionMeanMs,
        @PositiveOrZero Integer reactionLapses) {

    public CheckinRequest {
        symptoms = symptoms == null ? List.of() : List.copyOf(symptoms);
    }
}
