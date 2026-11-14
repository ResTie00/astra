package kg.astra.domain;

/**
 * Expected solar particle event inside the planning horizon (in slots).
 *
 * @param peakProtonFluxPfu proton flux at peak, >=10 MeV channel, pfu
 */
public record SpaceWeatherForecast(boolean stormExpected, int onsetSlot, int peakSlot,
                                   int durationSlots, double peakProtonFluxPfu) {

    public static SpaceWeatherForecast quiet() {
        return new SpaceWeatherForecast(false, 0, 0, 0, 0.0);
    }

    /** True if the slot falls inside the expected storm window. */
    public boolean covers(int slot) {
        return stormExpected && slot >= onsetSlot && slot < onsetSlot + durationSlots;
    }
}
