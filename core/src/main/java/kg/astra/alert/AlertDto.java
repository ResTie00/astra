package kg.astra.alert;

import java.time.Instant;

//crewId is null for mission-wide alerts (e.g. a storm)
public record AlertDto(long id, String crewId, AlertSeverity severity, String type,
                       String message, Instant createdAt) {}
