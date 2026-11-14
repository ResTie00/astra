package kg.astra.health;

import kg.astra.checkin.Checkin;
import kg.astra.checkin.CheckinRepository;
import kg.astra.telemetry.Metric;
import kg.astra.telemetry.TelemetryReading;
import kg.astra.telemetry.TelemetryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class TrendService {

    private final TelemetryRepository telemetryRepository;
    private final CheckinRepository checkinRepository;
    private final BaselineService baselineService;

    TrendsResponse trends(String crewId, int days) {
        Instant now = Instant.now();
        LocalDate today = LocalDate.ofInstant(now, ZoneOffset.UTC);
        LocalDate first = today.minusDays(days - 1L);
        Instant from = first.atStartOfDay(ZoneOffset.UTC).toInstant();

        Map<LocalDate, Double> sleep = telemetryRepository
                .findByCrewIdAndMetricAndRecordedAtAfterOrderByRecordedAt(crewId, Metric.SLEEP_HOURS, from).stream()
                .collect(Collectors.toMap(r -> day(r.getRecordedAt()), TelemetryReading::getValue, (a, b) -> b));

        // HRV: median of hourly medians per day, so dense live readings do not outweigh the rest of the day
        Map<LocalDate, Double> hrv = telemetryRepository
                .findByCrewIdAndMetricAndRecordedAtAfterOrderByRecordedAt(crewId, Metric.HRV_RMSSD, from).stream()
                .collect(Collectors.groupingBy(r -> day(r.getRecordedAt())))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> medianOfHourlyMedians(e.getValue())));

        // the last check-in of each day
        Map<LocalDate, Checkin> checkins = checkinRepository
                .findByCrewIdAndRecordedAtAfterOrderByRecordedAt(crewId, from).stream()
                .collect(Collectors.toMap(c -> day(c.getRecordedAt()), c -> c, (a, b) -> b));

        List<TrendsResponse.Point> points = new ArrayList<>();
        for (LocalDate d = first; !d.isAfter(today); d = d.plusDays(1)) {
            Checkin c = checkins.get(d);
            points.add(new TrendsResponse.Point(d,
                    round1(sleep.get(d)),
                    c == null || c.getReactionMeanMs() == null ? null : c.getReactionMeanMs().doubleValue(),
                    round1(hrv.get(d)),
                    c == null ? null : c.getWellbeing()));
        }

        Map<Metric, Baseline> baselines = baselineService.baselineOf(crewId, now);
        return new TrendsResponse(crewId, points,
                band(baselines.get(Metric.SLEEP_HOURS)),
                band(baselineService.reactionBaseline(crewId, now)),
                dailyBand(hrv, today));
    }

    private static double medianOfHourlyMedians(List<TelemetryReading> readings) {
        double[] hourly = readings.stream()
                .collect(Collectors.groupingBy(r -> r.getRecordedAt().truncatedTo(ChronoUnit.HOURS)))
                .values().stream()
                .mapToDouble(h -> median(h.stream().mapToDouble(TelemetryReading::getValue).toArray()))
                .toArray();
        return median(hourly);
    }

    private static double median(double[] values) {
        double[] sorted = values.clone();
        Arrays.sort(sorted);
        return BaselineService.median(sorted);
    }

    /** Same scale as Baseline.z: max(1.4826·MAD, 2% of the median). Null when there is no norm yet. */
    private static TrendsResponse.Band band(Baseline b) {
        if (b == null || b.samples() == 0) return null;
        double scale = Math.max(1.4826 * b.mad(), 0.02 * Math.abs(b.median()));
        return new TrendsResponse.Band(round1(b.median()), round1(b.median() - 2 * scale), round1(b.median() + 2 * scale));
    }

    private static LocalDate day(Instant t) {
        return LocalDate.ofInstant(t, ZoneOffset.UTC);
    }

    private static Double round1(Double v) {
        return v == null ? null : Math.round(v * 10) / 10.0;
    }
    /** Norm band built from daily values, excluding the last 3 days (same window rule as the baseline). */
    private static TrendsResponse.Band dailyBand(Map<LocalDate, Double> daily, LocalDate today) {
        double[] values = daily.entrySet().stream()
                .filter(e -> e.getKey().isBefore(today.minusDays(2)))
                .mapToDouble(Map.Entry::getValue)
                .toArray();
        if (values.length < 3) return null;
        Baseline b = BaselineService.of("HRV_RMSSD", values);
        double scale = Math.max(1.4826 * b.mad(), 0.02 * Math.abs(b.median()));
        return new TrendsResponse.Band(round1(b.median()), round1(b.median() - 2 * scale), round1(b.median() + 2 * scale));
    }
}