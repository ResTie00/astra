package kg.astra.replan;

import jakarta.validation.constraints.NotBlank;

//POST /api/replan/{runId}/accept
public record AcceptPlanRequest(@NotBlank String optionId) {}
