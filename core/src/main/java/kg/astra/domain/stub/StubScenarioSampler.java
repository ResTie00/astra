package kg.astra.domain.stub;

import kg.astra.domain.ScenarioSampler;
import kg.astra.domain.SpaceWeatherForecast;
import kg.astra.domain.SpeScenario;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

/** Simple reproducible sampler around the forecast. Arsen replaces the distributions. */
public class StubScenarioSampler implements ScenarioSampler {

    @Override
    public List<SpeScenario> sample(SpaceWeatherForecast f, int runs, long seed) {
        RandomGenerator rng = RandomGeneratorFactory.of("L64X128MixRandom").create(seed);
        List<SpeScenario> out = new ArrayList<>(runs);
        for (int i = 0; i < runs; i++) {
            if (!f.stormExpected()) {
                out.add(new SpeScenario(0, 0, 0, 1.0));
                continue;
            }
            double flux = f.peakProtonFluxPfu() * Math.exp(rng.nextGaussian(0, 0.6));      // lognormal around forecast
            int onset = Math.max(0, f.onsetSlot() + (int) Math.round(rng.nextGaussian(0, 2)));
            int duration = Math.max(1, f.durationSlots() + (int) Math.round(rng.nextGaussian(0, 3)));
            double attenuation = Math.clamp(rng.nextGaussian(0.15, 0.03), 0.05, 0.4);
            out.add(new SpeScenario(flux, onset, duration, attenuation));
        }
        return List.copyOf(out);
    }
}
