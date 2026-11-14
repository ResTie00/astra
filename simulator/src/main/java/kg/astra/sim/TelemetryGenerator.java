package kg.astra.sim;

import kg.astra.sim.dto.CheckinRequest;
import kg.astra.sim.dto.EnvMetric;
import kg.astra.sim.dto.EnvironmentReadingDto;
import kg.astra.sim.dto.Metric;
import kg.astra.sim.dto.TelemetryReadingDto;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

/**
 * Turns a profile + scenario + moment in time into readings.
 * All randomness comes from one seeded generator, so the same seed gives the same data.
 */
public class TelemetryGenerator {

    /** PLACEHOLDER background dose rate, µSv/h. Replace with a sourced value (MSL RAD). */
    static final double BASE_DOSE_RATE = 75.0;

    static final List<String> MODULES = List.of("HAB", "LAB");

    private final Scenario scenario;
    private final RandomGenerator rng;

    public TelemetryGenerator(Scenario scenario, long seed) {
        this.scenario = scenario;
        this.rng = RandomGeneratorFactory.of("L64X128MixRandom").create(seed);
    }

    /** Wearable sensors: one value per metric at this moment. */
    public List<TelemetryReadingDto> vitals(CrewProfile p, Instant at) {
        double d = scenario.decline(p.id(), at);
        double day = dayRhythm(at);

        double heartRate = p.heartRate() + 6 * day + 5 * d + rng.nextGaussian(0, 2.5);
        double hrv = p.hrv() - 5 * day - 12 * d + rng.nextGaussian(0, 4);
        double spo2 = Math.clamp(p.spo2() + rng.nextGaussian(0, 0.5), 93, 100);
        double temp = p.bodyTemp() + 0.25 * day + rng.nextGaussian(0, 0.08);
        double dose = Math.max(0, BASE_DOSE_RATE + rng.nextGaussian(0, 4));

        List<TelemetryReadingDto> out = new ArrayList<>(5);
        out.add(new TelemetryReadingDto(p.id(), at, Metric.HEART_RATE, round1(heartRate)));
        out.add(new TelemetryReadingDto(p.id(), at, Metric.HRV_RMSSD, round1(Math.max(5, hrv))));
        out.add(new TelemetryReadingDto(p.id(), at, Metric.SPO2, round1(spo2)));
        out.add(new TelemetryReadingDto(p.id(), at, Metric.BODY_TEMP, round2(temp)));
        out.add(new TelemetryReadingDto(p.id(), at, Metric.DOSE_RATE, round1(dose)));
        return out;
    }

    /** Hours slept last night, reported once in the morning. */
    public TelemetryReadingDto sleep(CrewProfile p, Instant morning) {
        double d = scenario.decline(p.id(), morning);
        double deficit = d > 0 ? Scenario.SLEEP_DEFICIT_HOURS : 0;   // full deficit from the first bad night
        double hours = p.sleepHours() - deficit + rng.nextGaussian(0, 0.3);
        return new TelemetryReadingDto(p.id(), morning, Metric.SLEEP_HOURS, round1(Math.max(2, hours)));
    }

    /**
     * Morning check-in. In the decline the reaction test gets worse,
     * but the self-rating does NOT change: the person does not notice.
     */
    public CheckinRequest checkin(CrewProfile p, Instant at) {
        double d = scenario.decline(p.id(), at);
        int reaction = (int) Math.round(p.reactionMs() * (1 + Scenario.REACTION_SLOWDOWN * d) + rng.nextGaussian(0, 10));
        int lapses = (int) Math.max(0, Math.round(1 + 3 * d + rng.nextGaussian(0, 0.8)));
        boolean offDay = rng.nextDouble() < 0.15;
        // In the decline the self-rating stays at the usual level: the person feels fine.
        int wellbeing = d > 0 ? p.wellbeing() : clampScore(p.wellbeing() + (offDay ? -1 : 0));
        int mood = clampScore(4 + (rng.nextDouble() < 0.2 ? -1 : 0));
        int stress = clampScore(2 + (rng.nextDouble() < 0.2 ? 1 : 0));
        return new CheckinRequest(p.id(), at, wellbeing, mood, stress, List.of(), reaction, lapses);
    }

    /** Habitat sensors. CO2 is higher during the "day", when the crew is active. */
    public List<EnvironmentReadingDto> environment(Instant at) {
        double day = dayRhythm(at);
        List<EnvironmentReadingDto> out = new ArrayList<>();
        for (String module : MODULES) {
            double co2 = 2600 + 400 * day + rng.nextGaussian(0, 80);
            double temp = 22.5 + 0.5 * day + rng.nextGaussian(0, 0.2);
            double humidity = Math.clamp(45 + rng.nextGaussian(0, 2), 25, 75);
            out.add(new EnvironmentReadingDto(module, at, EnvMetric.CO2, Math.round(co2)));
            out.add(new EnvironmentReadingDto(module, at, EnvMetric.CABIN_TEMP, round1(temp)));
            out.add(new EnvironmentReadingDto(module, at, EnvMetric.HUMIDITY, round1(humidity)));
        }
        return out;
    }

    /** Circadian rhythm: -1 in the middle of the night, +1 in the afternoon (15:00 UTC = ship time). */
    private static double dayRhythm(Instant at) {
        var t = at.atZone(ZoneOffset.UTC);
        double hour = t.getHour() + t.getMinute() / 60.0;
        return Math.cos(2 * Math.PI * (hour - 15) / 24);
    }

    private static int clampScore(int v) {
        return Math.clamp(v, 1, 5);
    }

    private static double round1(double v) {
        return Math.round(v * 10) / 10.0;
    }

    private static double round2(double v) {
        return Math.round(v * 100) / 100.0;
    }
}
