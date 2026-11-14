package kg.astra.telemetry;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class telemetryService {
    private final TelemetryRepository telemetryRepository;

    @Transactional
    void saveTelemetry(TelemetryBatchRequest telemetryBatchRequest) {

    }
}
