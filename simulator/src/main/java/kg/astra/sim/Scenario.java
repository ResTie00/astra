package kg.astra.sim;

import java.time.Duration;
import java.time.Instant;

/**
 * What is injected into the data.
 *
 * "Quiet decline": one crew member sleeps less for the last days; heart rate, HRV and
 * reaction time drift, but their self-rating stays the same. This is the blind spot.
 */
public record Scenario(String declineCrewId, Instant declineStart) {

    public static final double SLEEP_DEFICIT_HOURS = 1.5;
    public static final double REACTION_SLOWDOWN = 0.18;   // +18% at full decline
    private static final double RAMP_DAYS = 3.0;

    /** 0 = healthy, 1 = full decline. Grows linearly over RAMP_DAYS and then stays at 1. */
    public double decline(String crewId, Instant at) {
        if (!crewId.equals(declineCrewId) || at.isBefore(declineStart)) return 0;
        double days = Duration.between(declineStart, at).toMinutes() / (60.0 * 24.0);
        return Math.min(1.0, (days + 1.0) / RAMP_DAYS);
    }
}
