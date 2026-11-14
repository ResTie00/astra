package kg.astra.domain;

/**
 * Owner: Arsen (package kg.astra.risk, plain Java, no Spring).
 * One model, two forms, so the solver and Monte Carlo always agree.
 */
public interface DoseModel {

    /** Expected dose of one slot of this task type, integer µSv. Used to build the solver's DoseTable. */
    long expectedDoseMicroSv(TaskType type, int slot, SpaceWeatherForecast forecast);

    /** Dose of one slot of this task type in a concrete storm scenario, µSv. Used by Monte Carlo. */
    double sampleDoseMicroSv(TaskType type, int slot, SpeScenario scenario);
}
