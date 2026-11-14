package kg.astra.risk;

import kg.astra.domain.SpaceWeatherForecast;
import kg.astra.domain.SpeScenario;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

/**
 * Draws random storm scenarios around the forecast.
 * Scenarios are drawn SEQUENTIALLY from one seeded generator, so a run is repeatable,
 * and the same list is reused for every candidate plan (fair comparison).
 */
public class SpeScenarioSampler {

    /**
     * @param fluxLogSigma     spread of the flux: flux = forecast * exp(sigma * gaussian).
     *                         Proton flux varies by orders of magnitude, so log-normal is natural.
     * @param onsetJitterSlots std deviation (slots) of the storm onset shift
     * @param attenuationSigma std deviation of shelter attenuation around its nominal value
     */
    public record Params(double fluxLogSigma, double onsetJitterSlots, double attenuationSigma) {}

    private final SpaceWeatherForecast forecast;
    private final double nominalAttenuation;
    private final Params p;

    public SpeScenarioSampler(SpaceWeatherForecast forecast, double nominalAttenuation, Params params) {
        this.forecast = forecast;
        this.nominalAttenuation = nominalAttenuation;
        this.p = params;
    }

    /** Draw n scenarios with a fixed seed. */
    public List<SpeScenario> sampleAll(int n, long seed) {
        RandomGenerator rng = RandomGeneratorFactory.of("L64X128MixRandom").create(seed);
        List<SpeScenario> out = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            out.add(sample(rng));
        }
        return out;
    }

    /** One scenario. Quiet forecast => a scenario with no storm at all. */
    public SpeScenario sample(RandomGenerator rng) {
        if (!forecast.stormExpected()) {
            return new SpeScenario(0.0, 0, 0, nominalAttenuation);
        }
        double flux = forecast.peakProtonFluxPfu() * Math.exp(p.fluxLogSigma() * rng.nextGaussian());
        int onset = Math.max(0, forecast.onsetSlot()
                + (int) Math.round(p.onsetJitterSlots() * rng.nextGaussian()));
        double att = clamp01(nominalAttenuation + p.attenuationSigma() * rng.nextGaussian());
        return new SpeScenario(flux, onset, forecast.durationSlots(), att);
    }

    private static double clamp01(double x) {
        return Math.max(0.0, Math.min(1.0, x));
    }
}


