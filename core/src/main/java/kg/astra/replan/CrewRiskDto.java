package kg.astra.replan;

public record CrewRiskDto(String crewId, String crewName, double doseP50MilliSv, double doseP95MilliSv,
                          double exceedBudgetProbability) {}
