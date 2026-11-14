package kg.astra.telemetry;

/** Crew telemetry metrics. Units are fixed by contract. */
public enum Metric {
    HEART_RATE("bpm"),
    HRV_RMSSD("ms"),
    SPO2("%"),
    BODY_TEMP("°C"),
    /** Sent once per day, value = hours slept last night. */
    SLEEP_HOURS("h"),
    /** Personal dosimeter reading. */
    DOSE_RATE("µSv/h");

    private final String unit;

    Metric(String unit) { this.unit = unit; }

    public String unit() { return unit; }
}
