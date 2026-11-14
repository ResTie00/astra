package kg.astra.domain;

/** Monte Carlo result for one crew member. Doses in µSv over the horizon. */
public record CrewRisk(String crewId, double doseP50MicroSv, double doseP95MicroSv,
                       double exceedBudgetProbability) {}
