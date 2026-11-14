package kg.astra.replan;

import java.time.Instant;
import java.util.List;

//Response of POST /api/replan: current plan with its risk + 2-3 options to choose from
public record ReplanResponse(String runId, Instant horizonStart, int horizonHours,
                             PlanDto currentPlan, RiskDto currentRisk, List<PlanOptionDto> options) {}
