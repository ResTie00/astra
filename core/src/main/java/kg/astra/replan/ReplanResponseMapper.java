package kg.astra.replan;

import kg.astra.domain.Assignment;
import kg.astra.domain.ChangeReason;
import kg.astra.domain.CrewMember;
import kg.astra.domain.CrewRisk;
import kg.astra.domain.MissionState;
import kg.astra.domain.PlanChange;
import kg.astra.domain.PlanOption;
import kg.astra.domain.PlanStrategy;
import kg.astra.domain.RiskReport;
import kg.astra.domain.Schedule;
import kg.astra.domain.Task;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Domain result -> API response. The mirror of MissionStateAssembler:
 * slots -> dates, µSv -> mSv, reason codes -> human text. Uses the SAME horizonStart.
 */
@Component
class ReplanResponseMapper {

    private static final DateTimeFormatter UTC_TIME =
            DateTimeFormatter.ofPattern("MMM d, HH:mm 'UTC'", Locale.US).withZone(ZoneOffset.UTC);

    ReplanResponse toResponse(String runId, MissionState state, Instant horizonStart,
                              RiskReport currentRisk, List<PlanOption> options, List<RiskReport> optionRisks) {
        Map<String, Task> tasks = state.taskById();
        Map<String, CrewMember> crew = state.crewById();

        List<PlanOptionDto> optionDtos = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            PlanOption o = options.get(i);
            List<PlanChangeDto> changes = o.changes().stream()
                    .map(c -> toChange(c, tasks, crew, horizonStart))
                    .toList();
            optionDtos.add(new PlanOptionDto("opt-" + (i + 1), o.strategy(), title(o.strategy()),
                    toPlan(o.schedule(), tasks, crew, horizonStart), changes,
                    toRisk(optionRisks.get(i), crew), o.status()));
        }

        return new ReplanResponse(runId, horizonStart, state.horizon().slots(),
                toPlan(state.currentSchedule(), tasks, crew, horizonStart),
                toRisk(currentRisk, crew), optionDtos);
    }

    private static PlanDto toPlan(Schedule schedule, Map<String, Task> tasks, Map<String, CrewMember> crew,
                                  Instant horizonStart) {
        List<AssignmentDto> items = schedule.assignments().stream()
                .filter(a -> tasks.containsKey(a.taskId()))
                .map(a -> toAssignment(a, tasks.get(a.taskId()), crew, horizonStart))
                .sorted(Comparator.comparing(AssignmentDto::start))
                .toList();
        return new PlanDto(items);
    }

    private static AssignmentDto toAssignment(Assignment a, Task task, Map<String, CrewMember> crew,
                                              Instant horizonStart) {
        Instant start = toInstant(horizonStart, a.startSlot());
        return new AssignmentDto(task.id(), task.name(), task.type(), a.crewId(), name(crew, a.crewId()),
                start, start.plus(task.durationSlots(), ChronoUnit.HOURS));
    }

    private static PlanChangeDto toChange(PlanChange c, Map<String, Task> tasks, Map<String, CrewMember> crew,
                                          Instant horizonStart) {
        Task task = tasks.get(c.taskId());
        String taskName = task != null ? task.name() : c.taskId();
        Instant from = c.fromStartSlot() == null ? null : toInstant(horizonStart, c.fromStartSlot());
        Instant to = c.toStartSlot() == null ? null : toInstant(horizonStart, c.toStartSlot());
        return new PlanChangeDto(c.taskId(), taskName, c.type(), c.fromCrewId(), c.toCrewId(), from, to,
                c.reason(), explain(c, taskName, crew, to));
    }

    /** "<what happened>: <why>", e.g. "Antenna repair given to Alex Rivera at Nov 15, 16:00 UTC: Maya Chen shows signs of fatigue". */
    private static String explain(PlanChange c, String taskName, Map<String, CrewMember> crew, Instant to) {
        String toName = name(crew, c.toCrewId());
        String fromName = name(crew, c.fromCrewId());
        String when = to == null ? "" : UTC_TIME.format(to);

        String what = switch (c.type()) {
            case MOVED -> taskName + " moved to " + when;
            case REASSIGNED -> taskName + " given to " + toName;
            case MOVED_AND_REASSIGNED -> taskName + " given to " + toName + " at " + when;
            case DROPPED -> taskName + " cancelled";
            case ADDED -> taskName + " added";
        };
        String why = switch (c.reason() == null ? ChangeReason.OTHER : c.reason()) {
            case MOVED_AFTER_STORM_PEAK -> "after the storm peak";
            case MOVED_BEFORE_STORM_PEAK -> "ahead of the storm";
            case REASSIGNED_CREW_FATIGUE -> fromName + " shows signs of fatigue";
            case REASSIGNED_DOSE_BUDGET -> fromName + " is close to the dose budget";
            case DROPPED_NON_MANDATORY -> "no safe slot in the planning window";
            case ADDED_REST_WINDOW -> "rest window";
            case OTHER -> "";
        };
        return why.isEmpty() ? what : what + ": " + why;
    }

    private static RiskDto toRisk(RiskReport r, Map<String, CrewMember> crew) {
        List<CrewRiskDto> byCrew = r.byCrew().stream()
                .map(c -> toCrewRisk(c, crew))
                .toList();
        return new RiskDto(round1(r.riskScore()), round3(r.crewExceedProbability()),
                round1(r.impairedCriticalHours()), byCrew, r.runs());
    }

    private static CrewRiskDto toCrewRisk(CrewRisk c, Map<String, CrewMember> crew) {
        return new CrewRiskDto(c.crewId(), name(crew, c.crewId()),
                round2(c.doseP50MicroSv() / 1000.0), round2(c.doseP95MicroSv() / 1000.0),
                round3(c.exceedBudgetProbability()));
    }

    private static String title(PlanStrategy s) {
        return switch (s) {
            case MIN_DOSE -> "Lowest dose";
            case MIN_CHANGES -> "Fewest changes";
            case BALANCED -> "Balanced";
        };
    }

    private static String name(Map<String, CrewMember> crew, String id) {
        if (id == null) return null;
        CrewMember m = crew.get(id);
        return m != null ? m.name() : id;
    }

    static Instant toInstant(Instant horizonStart, int slot) {
        return horizonStart.plus(slot, ChronoUnit.HOURS);
    }

    private static double round1(double v) { return Math.round(v * 10) / 10.0; }
    private static double round2(double v) { return Math.round(v * 100) / 100.0; }
    private static double round3(double v) { return Math.round(v * 1000) / 1000.0; }
}
