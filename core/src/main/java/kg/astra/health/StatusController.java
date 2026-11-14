package kg.astra.health;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
class StatusController {
    private final StatusService statusService;
    private final TrendService trendService;

    @GetMapping("/crew/{id}/status")
    CrewStatusResponse getCrewStatus(@PathVariable("id") String crewId){
        return statusService.statusOf(crewId);
    }
    @GetMapping("/crew")
    List<CrewStatusResponse> crew() {
        return statusService.statusOfAll();
    }
    @GetMapping("/crew/{id}/trends")
    TrendsResponse trends(@PathVariable("id") String crewId,
                          @RequestParam(defaultValue = "14") int days) {
        return trendService.trends(crewId, Math.clamp(days, 1, 30));
    }
}
