package kg.astra.domain;

/**
 * One sampled solar particle event for Monte Carlo.
 *
 * @param peakFluxPfu        proton flux at peak, pfu
 * @param shelterAttenuation fraction of storm dose that still gets through the shelter (0..1)
 */
public record SpeScenario(double peakFluxPfu, int onsetSlot, int durationSlots, double shelterAttenuation) {

    public boolean covers(int slot) {
        return durationSlots > 0 && slot >= onsetSlot && slot < onsetSlot + durationSlots;
    }
}
