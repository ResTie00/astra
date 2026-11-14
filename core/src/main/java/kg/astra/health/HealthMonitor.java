package kg.astra.health;

import kg.astra.alert.AlertService;
import kg.astra.alert.AlertSeverity;
import kg.astra.common.SseHub;
import kg.astra.common.StreamEvent;
import kg.astra.common.StreamEventType;
import kg.astra.crew.Crew;
import kg.astra.crew.CrewRepository;
import kg.astra.domain.HealthStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// checks every crew member once a minute, so problems show up even if nobody opens the screen
// only ACT raises an alert. WATCH doesn't, because a storm puts everyone in WATCH
// and that's already one storm alert for the whole mission
// if someone's overall status changes, we send the new one to the UI
@Component
@RequiredArgsConstructor
@Slf4j
class HealthMonitor {

    private final CrewRepository crewRepository;
    private final StatusService statusService;
    private final AlertService alertService;
    private final SseHub sseHub;

    private final Map<String, HealthStatus> lastOverall = new ConcurrentHashMap<>();

    @Scheduled(initialDelayString = "${astra.monitor.initial-delay-ms:15000}",
               fixedDelayString = "${astra.monitor.interval-ms:60000}")
    void check() {
        for (Crew crew : crewRepository.findAll()) {
            try {
                checkOne(crew);
            } catch (RuntimeException e) {
                log.warn("Health check failed for {}: {}", crew.getId(), e.toString());   // one bad member must not stop the rest
            }
        }
    }

    private void checkOne(Crew crew) {
        CrewStatusResponse status = statusService.statusOf(crew.getId());

        for (AreaStatusDto area : status.areas()) {
            if (area.status() != HealthStatus.ACT) continue;
            Map<String, Object> evidence = new LinkedHashMap<>();
            evidence.put("area", area.area().name());
            evidence.put("explanation", area.explanation());
            if (area.area() == HealthArea.BEHAVIORAL && status.blindSpot() != null) {
                evidence.put("selfRating", status.blindSpot().selfRating());
                evidence.put("objectiveScore", status.blindSpot().objectiveScore());
                evidence.put("drivers", status.blindSpot().drivers());
            }
            alertService.raise(crew.getId(), AlertSeverity.CRITICAL, alertType(area.area()),
                    message(crew.getName(), area), evidence);
        }

        HealthStatus previous = lastOverall.put(crew.getId(), status.overall());
        // only send when the overall state actually changes, or the UI will keep refreshing on every poll
        if (previous != status.overall()) {
            sseHub.publish(StreamEvent.of(StreamEventType.STATUS_CHANGED, status));
        }
    }

    private static String alertType(HealthArea area) {
        return area == HealthArea.BEHAVIORAL ? "BLIND_SPOT" : area.name();
    }

    private static String message(String name, AreaStatusDto area) {
        return switch (area.area()) {
            case BEHAVIORAL -> name + ": data shows more fatigue than self-rating. " + area.explanation();
            default -> name + ": " + area.explanation();
        };
    }
}
