package kg.astra.telemetry;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TelemetryController {
    private final TelemetryService telemetryService;

    @PostMapping("/telemetry")
    ResponseEntity<Void> ingestTelemetry(@Valid @RequestBody TelemetryBatchRequest request){
        telemetryService.saveTelemetry(request);
        return ResponseEntity.accepted().build();
    }
    @PostMapping("/environment")
    ResponseEntity<Void> ingestEnvironment(@Valid @RequestBody EnvironmentBatchRequest request){
        telemetryService.saveEnvironment(request);
        return ResponseEntity.accepted().build();
    }
}
