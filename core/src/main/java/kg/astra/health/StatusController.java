package kg.astra.health;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
class StatusController {
    private final StatusService statusService;

    @GetMapping("/crew/{id}/status")
    CrewStatusResponse getCrewStatus(@PathVariable("id") String crewId){
        return statusService.statusOf(crewId);
    }
}
