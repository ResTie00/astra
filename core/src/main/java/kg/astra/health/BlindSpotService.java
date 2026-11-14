package kg.astra.health;

import kg.astra.checkin.Checkin;
import kg.astra.checkin.CheckinRepository;
import kg.astra.telemetry.Metric;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
class BlindSpotService {

    private final BaselineService baselineService;
    private final RecentValues recentValues;
    private final CheckinRepository checkinRepository;

    private static final int RECENT_DAYS = 3;
    private static final double WEIGHT = 0.5;          // how much one unit of badness lowers the score
    private static final double MAX_BADNESS = 3.0;     // one signal cannot dominate
    private static final double GAP_THRESHOLD = 1.5;   // flag when self-rating is this much above data
    private static final double DRIVER_THRESHOLD = 1.5;
    private static final int MIN_HOURLY_SAMPLES = 20;  // HRV: one value per hour in history
    private static final int MIN_DAILY_SAMPLES = 5;    // sleep, reaction: one per day

    private enum Bad { LOW, HIGH }

    private record Signal(String name, double value, Baseline baseline, Bad bad) {

        double z() {
            return baseline.z(value);
        }

        /** 0 when normal or better than normal; capped. */
        double badness() {
            double towardsBad = bad == Bad.LOW ? -z() : z();
            return Math.min(Math.max(0, towardsBad), MAX_BADNESS);
        }

        /** Human text in hours and percent, not in sigmas. */
        String text() {
            double median = baseline.median();
            return switch (name) {
                case "SLEEP_HOURS" -> String.format(Locale.US,
                        "Sleep %.1f h below your norm over the last 3 nights", median - value);
                case "REACTION_TIME" -> String.format(Locale.US,
                        "Reaction time %.0f%% slower than your norm", (value / median - 1) * 100);
                case "HRV_RMSSD" -> String.format(Locale.US,
                        "HRV %.0f%% below your norm over the last 24 h", (1 - value / median) * 100);
                default -> name + " outside your norm";
            };
        }
    }
    BlindSpotDto evaluate(String crewId, Instant now) {
        Optional<Checkin> latestCheckIn = checkinRepository.findTopByCrewIdOrderByRecordedAtDesc(crewId);
        int selfRating = latestCheckIn.map(Checkin::getWellbeing).orElse(0);

        Map<Metric, Baseline> metricBaselineMap = baselineService.baselineOf(crewId, now);
        Baseline reactionBaseline = baselineService.reactionBaseline(crewId, now);

        OptionalDouble recentSleepHours = recentValues.dailyMeanLastDays(crewId, Metric.SLEEP_HOURS, now, RECENT_DAYS);
        OptionalDouble recentReactionTime = recentValues.reactionMeanLastDays(crewId, now, RECENT_DAYS);
        OptionalDouble recentHrv = recentValues.hourlyMedianLast24h(crewId, Metric.HRV_RMSSD, now);

        List<Signal> signalList = new ArrayList<>();
        addSignal(signalList, "SLEEP_HOURS", recentSleepHours,
                metricBaselineMap.get(Metric.SLEEP_HOURS), Bad.LOW, MIN_DAILY_SAMPLES);
        addSignal(signalList, "REACTION_TIME", recentReactionTime,
                reactionBaseline, Bad.HIGH, MIN_DAILY_SAMPLES);
        addSignal(signalList, "HRV_RMSSD", recentHrv,
                metricBaselineMap.get(Metric.HRV_RMSSD), Bad.LOW, MIN_HOURLY_SAMPLES);

        if (signalList.size() < 2){
            return new BlindSpotDto(selfRating, 0,0,false, List.of());
        }

        double sumOfBadness = signalList.stream().mapToDouble(Signal::badness).sum();

        double objective = Math.clamp(5 - WEIGHT * sumOfBadness, 1.0, 5.0);

        double gap = selfRating > 0 ? selfRating - objective : 0;

        boolean flagged = selfRating > 0 && gap >= GAP_THRESHOLD;

        List<FactorDto> drivers = !flagged ? List.of() : signalList.stream().filter(s -> s.badness() >= DRIVER_THRESHOLD)
                .sorted(Comparator.comparing(Signal::badness).reversed())
                .limit(3)
                .map(s -> new FactorDto(s.name, round1(s.z()), s.text()))
                .toList();
        return new BlindSpotDto(selfRating, round1(objective), round1(gap), flagged, drivers);
    }

    // ---------------------------------------------------------------
    private void addSignal(List<Signal> signals, String name, OptionalDouble recent,
                           Baseline baseline, Bad bad, int minSamples) {
        if (recent.isEmpty() || baseline == null || baseline.samples() < minSamples) return;
        signals.add(new Signal(name, recent.getAsDouble(), baseline, bad));
    }
    // ----------------------------------------------------------------
    private static double round1(double v) {
        return Math.round(v * 10) / 10.0;
    }
}

