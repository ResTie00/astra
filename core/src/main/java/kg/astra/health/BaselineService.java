package kg.astra.health;

import kg.astra.checkin.Checkin;
import kg.astra.checkin.CheckinRepository;
import kg.astra.telemetry.Metric;
import kg.astra.telemetry.TelemetryReading;
import kg.astra.telemetry.TelemetryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class BaselineService {
    private final TelemetryRepository telemetryRepository;
    private final CheckinRepository checkinRepository;
    private static final int WINDOW_DAYS = 14;
    private static final int RECENT_DAYS = 3;

    public Map<Metric, Baseline> baselineOf(String crewId, Instant now){
        Instant to = now.minus(RECENT_DAYS, ChronoUnit.DAYS);
        Instant from = to.minus(WINDOW_DAYS, ChronoUnit.DAYS);

        List<TelemetryReading> list = telemetryRepository.findByCrewIdAndRecordedAtBetweenOrderByRecordedAt(crewId, from, to);

        Map<Metric, double[]> valuesByMetric = list.stream()
                .collect(Collectors.groupingBy(TelemetryReading::getMetric,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                reads -> reads.stream()
                                        .mapToDouble(TelemetryReading::getValue)
                                        .toArray()
                        )));

        Map<Metric, Baseline> baselines = new EnumMap<>(Metric.class);
        valuesByMetric.forEach((m, v)-> baselines.put(m, of(m.name(), v)));
        return baselines;
    }

    public Baseline reactionBaseline(String crewId, Instant now){
        Instant to = now.minus(RECENT_DAYS, ChronoUnit.DAYS);
        Instant from = to.minus(WINDOW_DAYS, ChronoUnit.DAYS);

        double[] values = checkinRepository
                .findByCrewIdAndRecordedAtBetweenOrderByRecordedAt(crewId, from, to).stream()
                .map(Checkin::getReactionMeanMs)
                .filter(Objects::nonNull)
                .mapToDouble(Integer::doubleValue)
                .toArray();
        return of("REACTION_TIME", values);
    }
    static Baseline of(String name, double[] values) {
        double[] sorted = values.clone();
        Arrays.sort(sorted);
        double median = median(sorted);
        double[] deviations = new double[sorted.length];
        for (int i = 0; i < sorted.length; i++) deviations[i] = Math.abs(sorted[i] - median);
        Arrays.sort(deviations);
        return new Baseline(name, median, median(deviations), sorted.length);
    }

    static double median(double[] sorted) {
        if (sorted.length == 0) return 0;
        int mid = sorted.length / 2;
        return sorted.length % 2 == 1 ? sorted[mid] : (sorted[mid - 1] + sorted[mid]) / 2;
    }
}
