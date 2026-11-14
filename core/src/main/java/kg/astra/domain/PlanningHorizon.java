package kg.astra.domain;


// Planning window. Time inside the solver is measured in integer slots
//counted from the start of the horizon. One slot = {@link #SLOT_MINUTES} minutes.
//Only Core converts slots to real dates and back.

public record PlanningHorizon(int slots, double solverTimeLimitSeconds, long seed) {

    public static final int SLOT_MINUTES = 60;

    public PlanningHorizon {
        if (slots <= 0) throw new IllegalArgumentException("slots must be > 0");
        if (solverTimeLimitSeconds <= 0) throw new IllegalArgumentException("solverTimeLimitSeconds must be > 0");
    }
}
