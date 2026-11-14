package kg.astra.health;

import kg.astra.checkin.Checkin;
import kg.astra.checkin.CheckinRepository;
import kg.astra.crew.Crew;
import kg.astra.crew.CrewRepository;
import kg.astra.domain.HealthStatus;
import kg.astra.spaceweather.SpaceWeatherEvent;
import kg.astra.spaceweather.SpaceWeatherService;
import kg.astra.telemetry.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static kg.astra.domain.HealthStatus.*;

@Service
@RequiredArgsConstructor
public class StatusService {
    private final CrewRepository crewRepository;
    private final TelemetryRepository telemetryRepository;
    private final EnvironmentRepository environmentRepository;
    private final CheckinRepository checkinRepository;
    private final BaselineService baselineService;
    private final RecentValues recentValues;
    private final BlindSpotService blindSpotService;
    private final SpaceWeatherService spaceWeatherService;

    private static final double DOSE_WATCH_FRACTION = 0.60;
    private static final double DOSE_ACT_FRACTION = 0.85;
    private static final double CO2_WATCH_PPM = 4000;
    private static final double CO2_ACT_PPM = 6000;
    private static final double FEVER_WATCH_C = 37.5;
    private static final double FEVER_ACT_C = 38.0;
    private static final int MIN_HOURLY_SAMPLES = 20;

    private static final DateTimeFormatter UTC_TIME =
            DateTimeFormatter.ofPattern("MMM d, HH:mm 'UTC'", Locale.US).withZone(ZoneOffset.UTC);


    public CrewStatusResponse statusOf(String crewId){
        Crew astronaut = crewRepository.findById(crewId).orElseThrow(() -> new NoSuchElementException("Unknown crew member: " + crewId));
        Instant now = Instant.now();

        Map<Metric, Baseline> baselines = baselineService.baselineOf(crewId, now);

        BlindSpotDto blindSpot = blindSpotService.evaluate(crewId, now);

        Optional<SpaceWeatherEvent> storm = spaceWeatherService.current();
        double doseMicroSv = recentValues.cumulativeDoseMicroSv(crewId);

        List<AreaStatusDto> areas = List.of(
                radiation(doseMicroSv, astronaut.getDoseBudgetMicroSv(), storm),
                behavioral(blindSpot),
                cardiovascular(crewId, baselines, now),
                immune(crewId),
                boneMuscle(),
                environment()

        );

        HealthStatus overall = areas.stream()
                .map(AreaStatusDto::status)
                .reduce(NORMAL, StatusService::worst);

        RecommendedActionDto action  = recommend(areas, storm, now);

        return  new CrewStatusResponse(crewId, astronaut.getName(), now, overall, areas, blindSpot, action,
                round1(doseMicroSv / 1000.0), round1(astronaut.getDoseBudgetMicroSv() / 1000.0));

    }

    private AreaStatusDto radiation(double doseMicroSv, long budgetMicroSv, Optional<SpaceWeatherEvent> storm) {
        double fraction = budgetMicroSv > 0 ? doseMicroSv / budgetMicroSv : 0;
        HealthStatus status = fraction >= DOSE_ACT_FRACTION ? ACT
                : fraction >= DOSE_WATCH_FRACTION ? WATCH : NORMAL;
        String text = String.format(Locale.US, "%.0f%% of mission dose budget used", fraction * 100);

        if (storm.isPresent()) {
            status = worst(status, WATCH);
            text += ". Solar particle event expected " + UTC_TIME.format(storm.get().getOnset());
        }
        return new AreaStatusDto(HealthArea.RADIATION, status, text);
    }

    private AreaStatusDto behavioral(BlindSpotDto b) {
        if (b.flagged()) {
            String text = b.drivers().isEmpty()
                    ? "Your data shows more fatigue than you feel"
                    : b.drivers().getFirst().text();
            return new AreaStatusDto(HealthArea.BEHAVIORAL, ACT, text);
        }
        if (b.objectiveScore() > 0 && b.objectiveScore() < 3) {
            return new AreaStatusDto(HealthArea.BEHAVIORAL, WATCH, "Signs of fatigue in your data");
        }
        return new AreaStatusDto(HealthArea.BEHAVIORAL, NORMAL, "Sleep and reaction within your norm");
    }

    private AreaStatusDto cardiovascular(String crewId, Map<Metric, Baseline> baselines, Instant now) {
        double hrUp = Math.max(0, z(crewId, Metric.HEART_RATE, baselines, now));
        double hrvDown = Math.max(0, -z(crewId, Metric.HRV_RMSSD, baselines, now));
        HealthStatus status = fromZ(Math.max(hrUp, hrvDown));

        String text = status == NORMAL
                ? "Heart rate and HRV within your norm"
                : (hrvDown >= hrUp ? "HRV below your norm" : "Heart rate above your norm") + " over the last 24 h";
        return new AreaStatusDto(HealthArea.CARDIOVASCULAR, status, text);
    }

    /** z of the last-24h hourly median against the personal norm; 0 if not enough data. */
    private double z(String crewId, Metric metric, Map<Metric, Baseline> baselines, Instant now) {
        Baseline b = baselines.get(metric);
        OptionalDouble recent = recentValues.hourlyMedianLast24h(crewId, metric, now);
        if (b == null || recent.isEmpty() || b.samples() < MIN_HOURLY_SAMPLES) return 0;
        return b.z(recent.getAsDouble());
    }

    private AreaStatusDto immune(String crewId) {
        OptionalDouble temp = recentValues.hourlyMedianLast24h(crewId, Metric.BODY_TEMP, Instant.now());
        List<String> symptoms = checkinRepository.findTopByCrewIdOrderByRecordedAtDesc(crewId)
                .map(Checkin::symptomList)
                .orElse(List.of());

        double t = temp.orElse(0);
        HealthStatus status = t >= FEVER_ACT_C ? ACT : t >= FEVER_WATCH_C ? WATCH : NORMAL;
        if (!symptoms.isEmpty()) status = worst(status, WATCH);

        String text = !symptoms.isEmpty() ? "Reported: " + String.join(", ", symptoms)
                : status == NORMAL ? "Temperature within normal range"
                : String.format(Locale.US, "Temperature %.1f °C", t);
        return new AreaStatusDto(HealthArea.IMMUNE, status, text);
    }

    private AreaStatusDto boneMuscle() {
        return new AreaStatusDto(HealthArea.BONE_MUSCLE, NORMAL, "Exercise tracking not connected in this prototype");
    }

    private AreaStatusDto environment() {
        return environmentRepository.findLatestPerModuleAndMetric().stream()
                .filter(r -> r.getMetric() == EnvMetric.CO2)
                .max(Comparator.comparingDouble(EnvironmentReading::getValue))
                .map(r -> {
                    double ppm = r.getValue();
                    HealthStatus status = ppm > CO2_ACT_PPM ? ACT : ppm > CO2_WATCH_PPM ? WATCH : NORMAL;
                    return new AreaStatusDto(HealthArea.ENVIRONMENT, status,
                            String.format(Locale.US, "CO2 %,.0f ppm in module %s", ppm, r.getModule()));
                })
                .orElse(new AreaStatusDto(HealthArea.ENVIRONMENT, NORMAL, "No habitat data yet"));
    }

    private static HealthStatus fromZ(double z) {
        if (z >= 3) return ACT;
        if (z >= 2) return WATCH;
        return NORMAL;
    }

    /** Enum order is NORMAL, WATCH, ACT: a higher ordinal is worse. */
    private static HealthStatus worst(HealthStatus a, HealthStatus b) {
        return a.ordinal() >= b.ordinal() ? a : b;
    }

    private static double round1(double v) {
        return Math.round(v * 10) / 10.0;
    }

    private RecommendedActionDto recommend(List<AreaStatusDto> areas, Optional<SpaceWeatherEvent> storm, Instant now) {
        Map<HealthArea, HealthStatus> s = areas.stream()
                .collect(Collectors.toMap(AreaStatusDto::area, AreaStatusDto::status));

        if (storm.isPresent() && !storm.get().getOnset().isAfter(now)) {
            return new RecommendedActionDto(ActionType.SHELTER, "Solar storm in progress: move to the storm shelter");
        }
        if (s.get(HealthArea.BEHAVIORAL) == ACT) {
            return new RecommendedActionDto(ActionType.REPLAN, "Move upcoming EVA and add a sleep window");
        }
        if (storm.isPresent()) {
            return new RecommendedActionDto(ActionType.REPLAN, "Solar storm expected: review upcoming EVA");
        }
        if (s.get(HealthArea.IMMUNE) != NORMAL) {
            return new RecommendedActionDto(ActionType.CONTACT_MEDIC, "Report your symptoms to the medical officer");
        }
        if (s.get(HealthArea.CARDIOVASCULAR) != NORMAL) {
            return new RecommendedActionDto(ActionType.SLEEP, "Prioritise sleep tonight");
        }
        return new RecommendedActionDto(ActionType.NONE, "Keep your current plan");
    }
    public HealthStatus statusFor(String crewId) {
        return statusOf(crewId).overall();
    }
}
