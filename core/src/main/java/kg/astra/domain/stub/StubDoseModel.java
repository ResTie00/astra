package kg.astra.domain.stub;

import kg.astra.domain.DoseModel;
import kg.astra.domain.SpaceWeatherForecast;
import kg.astra.domain.SpeScenario;
import kg.astra.domain.TaskType;


//PLACEHOLDER numbers so the system runs end to end. Arsen replaces them with
//sourced values. Do not put these numbers on a slide.


public class StubDoseModel implements DoseModel {

    @Override
    public long expectedDoseMicroSv(TaskType type, int slot, SpaceWeatherForecast f) {
        double storm = f.covers(slot) ? f.peakProtonFluxPfu() * shape(slot, f.onsetSlot(), f.peakSlot(), f.durationSlots()) : 0;
        return Math.round(base(type) + storm * stormFactor(type) * 0.05);
    }

    @Override
    public double sampleDoseMicroSv(TaskType type, int slot, SpeScenario s) {
        double storm = s.covers(slot)
                ? s.peakFluxPfu() * shape(slot, s.onsetSlot(), s.onsetSlot() + s.durationSlots() / 3, s.durationSlots())
                : 0;
        double factor = type == TaskType.SLEEP ? s.shelterAttenuation() : stormFactor(type);
        return base(type) + storm * factor * 0.05;
    }

    private static double base(TaskType t) {
        return switch (t) {
            case EVA -> 110;
            case INTERNAL, EXERCISE -> 75;
            case SLEEP -> 60;
        };
    }

    private static double stormFactor(TaskType t) {
        return switch (t) {
            case EVA -> 1.0;
            case INTERNAL, EXERCISE -> 0.3;
            case SLEEP -> 0.15;
        };
    }

    //Triangular storm profile: 0 at onset, 1 at peak, 0 at the end
    private static double shape(int slot, int onset, int peak, int duration) {
        int end = onset + duration;
        if (slot < onset || slot >= end) return 0;
        if (slot <= peak) return peak == onset ? 1 : (double) (slot - onset) / (peak - onset);
        return (double) (end - slot) / (end - peak);
    }
}
