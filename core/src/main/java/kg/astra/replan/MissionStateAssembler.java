package kg.astra.replan;

import kg.astra.crew.CrewRepository;
import kg.astra.domain.*;
import kg.astra.health.AreaStatusDto;
import kg.astra.health.CrewStatusResponse;
import kg.astra.health.HealthArea;
import kg.astra.health.StatusService;
import kg.astra.spaceweather.SpaceWeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
class MissionStateAssembler {
    private final CrewRepository crewRepository;
    private final ScheduleEntryRepository scheduleRepository;
    private final StatusService statusService;
    private final SpaceWeatherService spaceWeatherService;
    private final DoseModel doseModel;
    private final MissionTaskRepository taskRepository;

    @Value("${astra.replan.horizon-hours}")
    private int horizonHours;

    @Value("${astra.replan.solver-time-limit-seconds}")
    private double solverTimeLimitSeconds;

    @Value("${astra.replan.seed}")
    private long seed;

    MissionState assemble(Instant horizonStart){
        PlanningHorizon horizon = new PlanningHorizon(horizonHours, solverTimeLimitSeconds, seed);
        Instant horizonEnd = horizonStart.plus(horizonHours, ChronoUnit.HOURS);

        List<CrewMember > crew = crew();
        List<Task> tasks = tasks(horizonStart, horizonEnd);
        Schedule current = currentSchedule(horizonStart, tasks);
        SpaceWeatherForecast forecast = spaceWeatherService.toForecast(horizonStart, horizonHours);
        DoseTable doseTable = DoseTable.build(doseModel, forecast, horizonHours);
        return new MissionState(horizon, crew, tasks, current, doseTable, forecast);

    }
    private List<CrewMember> crew() {
        return crewRepository.findAll().stream()
                .map(c -> {
                    CrewStatusResponse status = statusService.statusOf(c.getId());
                    long usedMicroSv = Math.round(status.cumulativeDoseMilliSv() * 1000);
                    long remaining = Math.max(0, c.getDoseBudgetMicroSv() - usedMicroSv);
                    return new CrewMember(c.getId(), c.getName(), c.skillSet(), remaining, fitness(status));
                })
                .toList();
    }

    /**
     * Personal condition only: behavioral, cardiovascular, immune.
     * Radiation is left out on purpose: the storm and dose reach the solver through the forecast
     * and the dose table, so a storm must not make every crew member look "tired".
     */
    private static HealthStatus fitness(CrewStatusResponse status) {
        return status.areas().stream()
                .filter(a -> a.area() == HealthArea.BEHAVIORAL
                        || a.area() == HealthArea.CARDIOVASCULAR
                        || a.area() == HealthArea.IMMUNE)
                .map(AreaStatusDto::status)
                .reduce(HealthStatus.NORMAL, (x, y) -> x.ordinal() >= y.ordinal() ? x : y);
    }
    private List<Task> tasks(Instant horizonStart, Instant horizonEnd) {
        List<Task> result = new ArrayList<>();
        for (MissionTask t : taskRepository.findAll()) {
            if (!t.getLatestEnd().isAfter(horizonStart)) continue;      // window already over

            int earliest = Math.max(0, toSlot(horizonStart, t.getEarliestStart()));
            int latestEnd = Math.min(horizonHours, toSlot(horizonStart, t.getLatestEnd()));

            if (latestEnd - earliest < t.getDurationHours()) {
                log.warn("Task {} does not fit into the horizon, skipped", t.getId());
                continue;
            }
            result.add(new Task(t.getId(), t.getName(), t.getType(), t.getDurationHours(),
                    t.getRequiredSkill(), t.isMandatory(), earliest, latestEnd));
        }
        return result;
    }
    /** Whole hours from the horizon start. Demo data sits on hour boundaries, so floor is exact. */
    static int toSlot(Instant horizonStart, Instant t) {
        return (int) Math.floorDiv(Duration.between(horizonStart, t).toMinutes(), 60);
    }
    private Schedule currentSchedule(Instant horizonStart, List<Task> tasks) {
        Set<String> taskIds = tasks.stream().map(Task::id).collect(Collectors.toSet());
        List<Assignment> assignments = scheduleRepository.findAll().stream()
                .filter(e -> taskIds.contains(e.getTaskId()))
                .filter(e -> !e.getStartAt().isBefore(horizonStart))   // already started: not ours to move
                .map(e -> new Assignment(e.getTaskId(), e.getCrewId(), toSlot(horizonStart, e.getStartAt())))
                .toList();
        return new Schedule(assignments);
    }
}

