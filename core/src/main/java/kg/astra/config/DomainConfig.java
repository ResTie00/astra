package kg.astra.config;

import kg.astra.domain.DoseModel;
import kg.astra.domain.Replanner;
import kg.astra.domain.RiskEvaluator;
import kg.astra.domain.ScenarioSampler;
import kg.astra.domain.stub.StubDoseModel;
import kg.astra.domain.stub.StubReplanner;
import kg.astra.domain.stub.StubRiskEvaluator;
import kg.astra.domain.stub.StubScenarioSampler;
import kg.astra.planner.CpSatReplanner;
import kg.astra.risk.MonteCarloRiskEvaluator;
import kg.astra.risk.SimpleDoseModel;
import kg.astra.risk.SpeScenarioSampler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The only place where module implementations are plugged into Spring.
 * Mansur's and Arsen's code stays free of Spring annotations.
 * To switch from a stub to the real implementation, change one line here.
 */
@Configuration
public class DomainConfig {

    @Bean
    public DoseModel doseModel() {
        // PLACEHOLDER values, consistent with the simulator (75 µSv/h inside). Replace with sourced numbers.
        return new SimpleDoseModel(new SimpleDoseModel.Params(
                110, 75, 75, 60,   // background per 1 h slot: EVA, INTERNAL, EXERCISE, SLEEP
                0.05,              // storm µSv per slot per pfu, unshielded
                0.25));            // nominal habitat attenuation
    }

    @Bean
    public ScenarioSampler scenarioSampler() {
        return new SpeScenarioSampler(0.25, new SpeScenarioSampler.Params(0.6, 2.0, 0.03));
    }

    @Bean
    public RiskEvaluator riskEvaluator(DoseModel doseModel) {
        return new MonteCarloRiskEvaluator(doseModel);
    }

    @Bean
    public Replanner replanner() {
        return new CpSatReplanner();                 // -> Mansur's CP-SAT replanner
    }
}
