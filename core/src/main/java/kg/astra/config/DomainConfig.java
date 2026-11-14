package kg.astra.config;

import kg.astra.domain.DoseModel;
import kg.astra.domain.Replanner;
import kg.astra.domain.RiskEvaluator;
import kg.astra.domain.ScenarioSampler;
import kg.astra.domain.stub.StubDoseModel;
import kg.astra.domain.stub.StubReplanner;
import kg.astra.domain.stub.StubRiskEvaluator;
import kg.astra.domain.stub.StubScenarioSampler;
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
        return new StubDoseModel();                 // -> Arsen's real DoseModel
    }

    @Bean
    public ScenarioSampler scenarioSampler() {
        return new StubScenarioSampler();           // -> Arsen's sampler
    }

    @Bean
    public RiskEvaluator riskEvaluator(DoseModel doseModel) {
        return new StubRiskEvaluator(doseModel);    // -> Arsen's Monte Carlo
    }

    @Bean
    public Replanner replanner() {
        return new StubReplanner();                 // -> Mansur's CP-SAT replanner
    }
}
