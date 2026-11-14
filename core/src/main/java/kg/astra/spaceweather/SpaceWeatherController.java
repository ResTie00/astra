package kg.astra.spaceweather;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SpaceWeatherController {
    private final SpaceWeatherService spaceWeatherService;

    @PostMapping("/space-weather")
    ResponseEntity<Void> register(@Valid @RequestBody SpaceWeatherEventRequest request){
        spaceWeatherService.register(request);
        return ResponseEntity.accepted().build();
    }
    @GetMapping("/space-weather/current")
    ResponseEntity<SpaceWeatherEventRequest> current() {
        return spaceWeatherService.current()
                .map(e -> ResponseEntity.ok(new SpaceWeatherEventRequest(
                        e.getType(), e.getOnset(), e.getPeak(), e.getPeakProtonFluxPfu(), e.getDurationHours())))
                .orElse(ResponseEntity.noContent().build());
    }
}
