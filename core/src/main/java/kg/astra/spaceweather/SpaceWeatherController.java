package kg.astra.spaceweather;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

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
}
