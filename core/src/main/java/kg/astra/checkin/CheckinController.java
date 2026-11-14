package kg.astra.checkin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CheckinController {
    private final CheckinService checkinService;

    @PostMapping("/checkin")
    CheckinResponse checkin(@Valid @RequestBody CheckinRequest checkinRequest) {
        return checkinService.submit(checkinRequest);
    }
}
