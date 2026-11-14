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

/**
 * Creates alerts, stores them and pushes them to the UI over SSE.
 * Anti-spam: the same alert type for the same crew member (or mission-wide) at most once per COOLDOWN.
 * An alert people learn to ignore is worse than no alert.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private static final Duration COOLDOWN = Duration.ofHours(1);

    private final AlertRepository alertRepository;
    private final SseHub sseHub;
    private final JsonMapper jsonMapper;

    /**
     * @param crewId   null for a mission-wide alert (e.g. a storm)
     * @param evidence the data behind the alert (values, thresholds); stored as JSON, may be null
     * @return the new alert, or empty if the same alert was raised recently
     */
    @Transactional
    public Optional<AlertDto> raise(String crewId, AlertSeverity severity, String type,
                                    String message, Map<String, Object> evidence) {
        Instant since = Instant.now().minus(COOLDOWN);
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

    public List<AlertDto> recent() {
        return alertRepository.findTop50ByOrderByCreatedAtDesc().stream().map(Alert::toDto).toList();
    }

    @Transactional
    public void acknowledge(long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Unknown alert: " + id));
        alert.acknowledge();
    }
}
