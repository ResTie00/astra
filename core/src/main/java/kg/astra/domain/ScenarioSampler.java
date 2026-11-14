package kg.astra.domain;

import java.util.List;

// Called ONCE per replanning run; the same list is passed to every evaluation
//(common random numbers), so differences between plans are real, not sampling noise.
//Same seed must give the same list.

public interface ScenarioSampler {
    List<SpeScenario> sample(SpaceWeatherForecast forecast, int runs, long seed);
}
