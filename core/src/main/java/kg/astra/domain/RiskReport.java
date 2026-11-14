package kg.astra.domain;

import java.util.List;

/**
 * Risk of one schedule, evaluated over a fixed list of scenarios.
 *
 * @param crewExceedProbability  probability that at least one person exceeds their budget
 * @param impairedCriticalHours  hours of EVA done by crew with status WATCH or ACT
 * @param riskScore              single number 0..100 for sorting options (higher = worse)
 * @param runs                   number of scenarios used
 */
public record RiskReport(List<CrewRisk> byCrew, double crewExceedProbability, double impairedCriticalHours,
                         double riskScore, int runs) {

    public RiskReport {
        byCrew = byCrew == null ? List.of() : List.copyOf(byCrew);
    }
}
