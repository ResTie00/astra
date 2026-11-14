package kg.astra.domain;


//Machine-readable reason for a plan change. Core turns it into human text
// so the solver never produces UI strings.

public enum ChangeReason {
    MOVED_AFTER_STORM_PEAK,
    MOVED_BEFORE_STORM_PEAK,
    REASSIGNED_CREW_FATIGUE,
    REASSIGNED_DOSE_BUDGET,
    DROPPED_NON_MANDATORY,
    ADDED_REST_WINDOW,
    OTHER
}
