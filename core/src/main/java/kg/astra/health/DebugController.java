package kg.astra.health;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
class DebugController {
    private final BlindSpotService blindSpotService;

    @GetMapping("/blind-spot/{crewId}")
    BlindSpotDto blindSpot (@PathVariable String crewId){
        return blindSpotService.evaluate(crewId, Instant.now());
    }
}
