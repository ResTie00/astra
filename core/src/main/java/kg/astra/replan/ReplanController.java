package kg.astra.replan;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/replan")
@RequiredArgsConstructor
class ReplanController {
    private final ReplanOrchestratorService replanOrchestratorService;

    @PostMapping
    ReplanResponse replan(@RequestBody(required = false) ReplanStartRequest request){
        return replanOrchestratorService.replan(request == null? null : request.reason());
    }

    @PostMapping("/{runId}/accept")
    AcceptPlanResponse accept(@PathVariable String runId, @Valid @RequestBody AcceptPlanRequest request) {
        return replanOrchestratorService.accept(runId, request.optionId());
    }
}
