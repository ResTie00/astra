package kg.astra.domain;

import java.util.List;

/**
 * Owner: Arsen. Evaluates one schedule over the given scenarios.
 * Convention: a slot where a crew member has no assignment counts as INTERNAL.
 * Must be a pure function: no randomness inside, all randomness comes from the scenarios.
 */
public interface RiskEvaluator {
    RiskReport evaluate(Schedule plan, MissionState state, List<SpeScenario> scenarios);
}
