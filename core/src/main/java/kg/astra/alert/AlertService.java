package kg.astra.alert;

import kg.astra.common.SseHub;
import kg.astra.common.StreamEvent;
import kg.astra.common.StreamEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

// saves alerts and sends them to the UI right away.
// the same alert for the same person shows up at most once an hour:
// if alerts keep repeating, people start ignoring them, and that's worse than no alert
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private static final Duration COOLDOWN = Duration.ofHours(1);

    private final AlertRepository alertRepository;
    private final SseHub sseHub;
    private final JsonMapper jsonMapper;

    // crewId = null means the alert is for the whole mission, like a storm.
    // evidence = the numbers behind the alert, saved as JSON so the doctor can see why it fired.
    // returns empty if the same alert was already raised in the last hour
    @Transactional
    public Optional<AlertDto> raise(String crewId, AlertSeverity severity, String type,
                                    String message, Map<String, Object> evidence) {
        Instant since = Instant.now().minus(COOLDOWN);
        // two different queries: in SQL "crew_id = null" never matches, we need "is null"
        boolean recent = crewId == null
                ? alertRepository.existsByCrewIdIsNullAndTypeAndCreatedAtAfter(type, since)
                : alertRepository.existsByCrewIdAndTypeAndCreatedAtAfter(crewId, type, since);
        if (recent) return Optional.empty();

        String evidenceJson = evidence == null ? null : jsonMapper.writeValueAsString(evidence);
        Alert saved = alertRepository.save(new Alert(crewId, severity, type, message, evidenceJson));
        AlertDto dto = saved.toDto();
        sseHub.publish(StreamEvent.of(StreamEventType.ALERT, dto));
        log.info("Alert raised: {} {} {}", severity, type, crewId == null ? "mission" : crewId);
        return Optional.of(dto);
    }

    // last 50 for the alerts list when the screen opens. new ones come live over SSE
    public List<AlertDto> recent() {
        return alertRepository.findTop50ByOrderByCreatedAtDesc().stream().map(Alert::toDto).toList();
    }

    // no save() needed: inside a transaction hibernate notices the change and writes it on commit
    @Transactional
    public void acknowledge(long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Unknown alert: " + id));
        alert.acknowledge();
    }
}