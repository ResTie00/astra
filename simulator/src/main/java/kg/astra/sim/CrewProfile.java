package kg.astra.sim;

import java.util.List;

/**
 * Personal norms of one crew member. Everyone is different on purpose:
 * Astra compares a person with their own baseline, not with a population average.
 */
public record CrewProfile(String id, double heartRate, double hrv, double spo2, double bodyTemp,
                          double sleepHours, double reactionMs, int wellbeing) {

    /** Ids must match the crew seeded in Core (V2__seed_crew.sql). */
    public static List<CrewProfile> crew() {
        return List.of(
                new CrewProfile("c1", 58, 62, 98.0, 36.5, 7.4, 275, 4),
                new CrewProfile("c2", 64, 52, 97.5, 36.6, 7.2, 290, 4),
                new CrewProfile("c3", 70, 44, 97.0, 36.7, 6.8, 305, 4),
                new CrewProfile("c4", 61, 57, 98.5, 36.4, 7.6, 282, 5));
    }
}
