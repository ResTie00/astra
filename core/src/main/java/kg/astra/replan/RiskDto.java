package kg.astra.replan;

import java.util.List;

public record RiskDto(double riskScore, double crewExceedProbability, double impairedCriticalHours,
                      List<CrewRiskDto> byCrew, int runs) {}
