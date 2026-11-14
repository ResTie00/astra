package kg.astra.replan;

import kg.astra.domain.PlanStrategy;
import kg.astra.domain.SolverStatus;

import java.util.List;

public record PlanOptionDto(String optionId, PlanStrategy strategy, String title, PlanDto plan,
                            List<PlanChangeDto> changes, RiskDto risk, SolverStatus solverStatus) {}
