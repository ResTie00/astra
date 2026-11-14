package kg.astra.domain;

//Overall health status of a crew member. Computed by Core (personal baseline + ML)
public enum HealthStatus {
    // Within personal norm.
    NORMAL,
    // Noticeable deviation: solver should avoid loading this person (soft penalty)
    WATCH,
    // Strong deviation: no EVA or critical tasks (hard constraint)
    ACT
}
