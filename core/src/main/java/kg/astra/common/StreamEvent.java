package kg.astra.common;

import java.time.Instant;

/**
 * Every SSE message has this shape. The UI switches on `type`.
 * Payload per type: TELEMETRY -> TelemetryReadingDto, STATUS_CHANGED -> CrewStatusResponse,
 * ALERT -> AlertDto, SPACE_WEATHER -> SpaceWeatherEventRequest, REPLAN_READY -> ReplanResponse.
 */
public record StreamEvent<T>(StreamEventType type, Instant timestamp, T payload) {

    public static <T> StreamEvent<T> of(StreamEventType type, T payload) {
        return new StreamEvent<>(type, Instant.now(), payload);
    }
}
