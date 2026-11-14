package kg.astra.spaceweather;

import kg.astra.common.SseHub;
import kg.astra.common.StreamEvent;
import kg.astra.common.StreamEventType;
import kg.astra.domain.SpaceWeatherForecast;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SpaceWeatherService {
    private final SpaceWeatherRepository spaceWeatherRepository;
    private final SseHub sseHub;

    public void register(SpaceWeatherEventRequest request){
        SpaceWeatherEvent weatherEvent = SpaceWeatherEvent.from(request);
        spaceWeatherRepository.save(weatherEvent);
        sseHub.publish(StreamEvent.of(StreamEventType.SPACE_WEATHER, request));
    }

    public Optional<SpaceWeatherEvent> current(){
        return spaceWeatherRepository.findTopByOrderByReceivedAtDesc().filter(e -> e.end().isAfter(Instant.now()));
    }

    public SpaceWeatherForecast toForecast(Instant horizonStart, int horizonHours){
        return SpaceWeatherForecast.quiet();
    }


}
