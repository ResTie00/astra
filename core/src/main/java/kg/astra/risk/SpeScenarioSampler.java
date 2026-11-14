package kg.astra.risk;

import kg.astra.domain.ScenarioSampler;
import kg.astra.domain.SpaceWeatherForecast;
import kg.astra.domain.SpeScenario;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

/**
 * Draws random storm scenarios around a forecast.
 * The forecast is NOT stored in the object: a Spring bean is created once at startup,
 * but the forecast is known only at replanning time, so it comes in as a method parameter.
 * Scenarios are drawn sequentially from one seeded generator: same seed => same list.
 */
public class SpeScenarioSampler implements ScenarioSampler {

    /**
     * @param fluxLogSigma     spread of the flux: flux = forecast * exp(sigma * gaussian).
     *                         Proton flux varies by orders of magnitude, so log-normal is natural.
     * @param onsetJitterSlots std deviation (slots) of the storm onset shift
     * @param attenuationSigma std deviation of shelter attenuation around its nominal value
     */
    public record Params(double fluxLogSigma, double onsetJitterSlots, double attenuationSigma) {}

    private final double nominalAttenuation;
    private final Params p;

    public SpeScenarioSampler(double nominalAttenuation, Params params) {
        this.nominalAttenuation = nominalAttenuation;
        this.p = params;
    }

    @Override
    public List<SpeScenario> sample(SpaceWeatherForecast forecast, int runs, long seed) {
        RandomGenerator rng = RandomGeneratorFactory.of("L64X128MixRandom").create(seed);
        List<SpeScenario> out = new ArrayList<>(runs);
        for (int i = 0; i < runs; i++) {
            out.add(sampleOne(forecast, rng));
        }
        return out;
    }

    /** One scenario. Quiet forecast => a scenario with no storm at all. */
    private SpeScenario sampleOne(SpaceWeatherForecast forecast, RandomGenerator rng) {
        if (!forecast.stormExpected()) {
            return new SpeScenario(0.0, 0, 0, nominalAttenuation);
        }
        double flux = forecast.peakProtonFluxPfu() * Math.exp(p.fluxLogSigma() * rng.nextGaussian());
        int onset = Math.max(0, forecast.onsetSlot()
                + (int) Math.round(p.onsetJitterSlots() * rng.nextGaussian()));
        double attenuation = clamp01(nominalAttenuation + p.attenuationSigma() * rng.nextGaussian());
        return new SpeScenario(flux, onset, forecast.durationSlots(), attenuation);
    }

    private static double clamp01(double x) {
        return Math.max(0.0, Math.min(1.0, x));
    }
}

