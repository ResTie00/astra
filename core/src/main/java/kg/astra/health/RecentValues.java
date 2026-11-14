package kg.astra.health;

import kg.astra.checkin.Checkin;
import kg.astra.checkin.CheckinRepository;
import kg.astra.telemetry.Metric;
import kg.astra.telemetry.TelemetryReading;
import kg.astra.telemetry.TelemetryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class RecentValues {
    private final TelemetryRepository telemetryRepository;
    private final CheckinRepository checkinRepository;

    OptionalDouble hourlyMedianLast24h(String crewId, Metric metric, Instant now) {
        // live data comes every few seconds, so the hourly median keeps the signal stable and removes spikes
        Instant from = now.minus(Duration.ofHours(24));

        List<TelemetryReading> readingList = telemetryRepository
                .findByCrewIdAndMetricAndRecordedAtAfterOrderByRecordedAt(crewId, metric, from);
        if (readingList.isEmpty()) {
            return OptionalDouble.empty();
        }
        Map<Instant, List<TelemetryReading>> byHour = readingList.stream()
                .collect(Collectors.groupingBy(r -> r.getRecordedAt().truncatedTo(ChronoUnit.HOURS)));

        double[] hourlyMedians = byHour.values().stream()
                .mapToDouble(list -> calculateMedian(
                        list.stream().mapToDouble(TelemetryReading::getValue).toArray()
                )).toArray();
        return OptionalDouble.of(calculateMedian(hourlyMedians));
    }

    OptionalDouble dailyMeanLastDays(String crewId, Metric metric, Instant now, int days) {
        Instant from = now.minus(Duration.ofDays(days));
        List<TelemetryReading> readings = telemetryRepository
                .findAllByCrewIdAndMetricAndRecordedAtBetween(crewId, metric, from, now);
        return readings.stream()
                .mapToDouble(TelemetryReading::getValue)
                .average();
    }

    OptionalDouble reactionMeanLastDays(String crewId, Instant now, int days){
        Instant from = now.minus(Duration.ofDays(days));
        List<Checkin> checkins = checkinRepository
                .findByCrewIdAndRecordedAtBetweenOrderByRecordedAt(crewId, from, now);
        return checkins.stream()
                .map(Checkin::getReactionMeanMs)
                .filter(Objects::nonNull)
                .mapToDouble(Number::doubleValue)
                .average();
    }


    private double calculateMedian(double[] values) {
        Arrays.sort(values);
        int mid = values.length / 2;
        if (values.length % 2 == 0) {
            return (values[mid - 1] + values[mid]) / 2.0;
        }
        return values[mid];
    }

    // dose is tracked as hourly medians summed over time, because the stream is noisy and we care about the total exposure
    double cumulativeDoseMicroSv(String crewId) {
        return telemetryRepository
                .findByCrewIdAndMetricAndRecordedAtAfterOrderByRecordedAt(crewId, Metric.DOSE_RATE, Instant.EPOCH)
                .stream()
                .collect(Collectors.groupingBy(r -> r.getRecordedAt().truncatedTo(ChronoUnit.HOURS)))
                .values().stream()
                .mapToDouble(hour -> calculateMedian(hour.stream().mapToDouble(TelemetryReading::getValue).toArray()))
                .sum();
    }


}