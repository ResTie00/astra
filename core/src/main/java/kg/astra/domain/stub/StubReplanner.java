package kg.astra.domain.stub;

import kg.astra.domain.MissionState;
import kg.astra.domain.PlanOption;
import kg.astra.domain.PlanStrategy;
import kg.astra.domain.ReplanResult;
import kg.astra.domain.Replanner;
import kg.astra.domain.SolverStatus;

import java.util.List;

//Returns the current schedule unchanged. Lets Core and the UI work before CP-SAT is ready
public class StubReplanner implements Replanner {

    @Override
    public ReplanResult replan(MissionState state) {
        PlanOption same = new PlanOption(PlanStrategy.BALANCED, state.currentSchedule(), List.of(),
                SolverStatus.FEASIBLE, 0);
        return new ReplanResult(SolverStatus.FEASIBLE, List.of(same), 0.0);
    }
}
