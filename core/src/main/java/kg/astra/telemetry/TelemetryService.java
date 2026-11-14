package kg.astra.telemetry;

import kg.astra.common.SseHub;
import kg.astra.common.StreamEvent;
import kg.astra.common.StreamEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
class TelemetryService {
    private final TelemetryRepository telemetryRepository;
    private final EnvironmentRepository environmentRepository;
    private final SseHub sseHub;

    private static final int LIVE_BATCH_LIMIT = 50;


    @Transactional
    void saveTelemetry(TelemetryBatchRequest telemetryBatchRequest) {
        List<TelemetryReading> readings = telemetryBatchRequest.readings().stream()
                .map(TelemetryReading::from)
                .toList();
        telemetryRepository.saveAllAndFlush(readings);

        if (readings.size() <= LIVE_BATCH_LIMIT){
            sseHub.publish(StreamEvent.of(StreamEventType.TELEMETRY,telemetryBatchRequest.readings()));
        }
    }

    @Transactional
    void saveEnvironment(EnvironmentBatchRequest environmentBatchRequest) {
        List<EnvironmentReading> environmentReadings = environmentBatchRequest.readings().stream()
                .map(EnvironmentReading::from)
                .toList();
        environmentRepository.saveAll(environmentReadings);
    }
}
