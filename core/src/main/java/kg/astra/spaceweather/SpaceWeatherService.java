package kg.astra.spaceweather;

import kg.astra.common.SseHub;
import kg.astra.common.StreamEvent;
import kg.astra.common.StreamEventType;
import kg.astra.domain.SpaceWeatherForecast;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SpaceWeatherService {
    private final SpaceWeatherRepository spaceWeatherRepository;
    private final SseHub sseHub;

    public void register(SpaceWeatherEventRequest request) {
        SpaceWeatherEvent weatherEvent = SpaceWeatherEvent.from(request);
        spaceWeatherRepository.save(weatherEvent);
        sseHub.publish(StreamEvent.of(StreamEventType.SPACE_WEATHER, request));
    }

    public Optional<SpaceWeatherEvent> current() {
        return spaceWeatherRepository.findTopByOrderByReceivedAtDesc()
                .filter(e -> e.end().isAfter(Instant.now()));
    }

    /**
     * Current storm as slots of the planning horizon [horizonStart, horizonStart + horizonHours).
     * A storm that started before the horizon is clipped to slot 0; one that ends after it, to the last slot.
     */
    public SpaceWeatherForecast toForecast(Instant horizonStart, int horizonHours) {
        Optional<SpaceWeatherEvent> current = current();
        if (current.isEmpty()) return SpaceWeatherForecast.quiet();

        SpaceWeatherEvent e = current.get();
        int onset = slot(horizonStart, e.getOnset());
        int peak = slot(horizonStart, e.getPeak());
        int end = slot(horizonStart, e.end());                 // exclusive

        if (end <= 0 || onset >= horizonHours) return SpaceWeatherForecast.quiet();   // outside the horizon

        int clippedOnset = Math.max(0, onset);
        int clippedEnd = Math.min(horizonHours, end);
        int clippedPeak = Math.clamp(peak, clippedOnset, clippedEnd - 1);
        return new SpaceWeatherForecast(true, clippedOnset, clippedPeak, clippedEnd - clippedOnset,
                e.getPeakProtonFluxPfu());
    }

    /** Whole hours from the horizon start (floor, also for times before it). */
    private static int slot(Instant horizonStart, Instant t) {
        return (int) Math.floorDiv(Duration.between(horizonStart, t).toMinutes(), 60);
    }
}
