package kg.astra.risk;

import kg.astra.domain.DoseModel;
import kg.astra.domain.SpaceWeatherForecast;
import kg.astra.domain.SpeScenario;
import kg.astra.domain.TaskType;

/**
 * Dose of ONE slot (30 min) = background(type) + storm part.
 * Storm part = coefficient * proton flux, reduced by shelter attenuation for everything except EVA.
 * Simplification: flux is constant over the storm window (peak value = conservative).
 * Dose of a whole task = sum over its slots (done by whoever builds the DoseTable).
 */
public class SimpleDoseModel implements DoseModel {

    /**
     * No default values on purpose: take numbers from open NASA sources, cite them in README.
     *
     * @param backgroundEva/Internal/Exercise/Sleep background dose per slot, microSv
     * @param stormMicroSvPerPfuPerSlot             storm dose per slot per 1 pfu, unshielded (EVA)
     * @param nominalShelterAttenuation             fraction (0..1) of storm dose that gets through
     *                                              the habitat, used for the expected (solver) dose
     */
    public record Params(long backgroundEva, long backgroundInternal,
                         long backgroundExercise, long backgroundSleep,
                         double stormMicroSvPerPfuPerSlot, double nominalShelterAttenuation) {}

    private final Params p;

    public SimpleDoseModel(Params params) {
        this.p = params;
    }

    /** Solver form: integer dose from the forecast (CP-SAT needs integers). */
    @Override
    public long expectedDoseMicroSv(TaskType type, int slot, SpaceWeatherForecast forecast) {
        double storm = forecast.covers(slot)
                ? stormDose(type, forecast.peakProtonFluxPfu(), p.nominalShelterAttenuation())
                : 0.0;
        return background(type) + Math.round(storm);
    }

    /** Monte Carlo form: exact (non-rounded) dose in one random storm. */
    @Override
    public double sampleDoseMicroSv(TaskType type, int slot, SpeScenario scenario) {
        double storm = scenario.covers(slot)
                ? stormDose(type, scenario.peakFluxPfu(), scenario.shelterAttenuation())
                : 0.0;
        return background(type) + storm;
    }

    /** EVA has no shelter (full dose); every other task happens inside, so attenuation applies. */
    private double stormDose(TaskType type, double fluxPfu, double attenuation) {
        double full = p.stormMicroSvPerPfuPerSlot() * fluxPfu;
        return type == TaskType.EVA ? full : full * attenuation;
    }

    private long background(TaskType type) {
        return switch (type) {
            case EVA -> p.backgroundEva();
            case INTERNAL -> p.backgroundInternal();
            case EXERCISE -> p.backgroundExercise();
            case SLEEP -> p.backgroundSleep();
        };
    }
}

