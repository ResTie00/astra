package kg.astra.alert;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
class AlertController {

    private final AlertService alertService;

    //Latest 50 alerts, newest first. Live alerts also arrive over SSE as ALERT events
    @GetMapping
    List<AlertDto> recent() {
        return alertService.recent();
    }

    @PostMapping("/{id}/ack")
    ResponseEntity<Void> acknowledge(@PathVariable long id) {
        alertService.acknowledge(id);
        return ResponseEntity.noContent().build();
    }
}
