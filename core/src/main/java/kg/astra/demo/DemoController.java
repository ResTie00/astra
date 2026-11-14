package kg.astra.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Reloads the demo mission relative to the current hour. Call it right before presenting. */
@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
class DemoController {

    private final DemoDataLoader loader;

    @PostMapping("/reset")
    ResponseEntity<Void> reset() {
        loader.reload();
        return ResponseEntity.noContent().build();
    }
}
