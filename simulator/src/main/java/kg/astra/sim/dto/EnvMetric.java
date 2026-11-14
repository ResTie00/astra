package kg.astra.sim.dto;

/** Habitat module environment metrics. */
public enum EnvMetric {
    CO2("ppm"),
    CABIN_TEMP("°C"),
    HUMIDITY("%");

    private final String unit;

    EnvMetric(String unit) { this.unit = unit; }

    public String unit() { return unit; }
}
