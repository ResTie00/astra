package kg.astra.planner;

import kg.astra.domain.Assignment;
import kg.astra.domain.ChangeReason;
import kg.astra.domain.ChangeType;
import kg.astra.domain.CrewMember;
import kg.astra.domain.HealthStatus;
import kg.astra.domain.MissionState;
import kg.astra.domain.PlanChange;
import kg.astra.domain.Schedule;
import kg.astra.domain.SpaceWeatherForecast;
import kg.astra.domain.Task;
import kg.astra.domain.TaskType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Compares a proposed schedule with state.currentSchedule().
 * Produces machine-readable changes only (ChangeReason codes); Core makes the human text.
 */
final class PlanDiff {

    private PlanDiff() {}

    static List<PlanChange> diff(MissionState state, Schedule next) {
        Map<String, Task> tasks = state.taskById();
        Map<String, CrewMember> crew = state.crewById();
        List<PlanChange> changes = new ArrayList<>();

        for (Assignment n : next.assignments()) {
            Task task = tasks.get(n.taskId());
            Optional<Assignment> oldOpt = state.currentSchedule().find(n.taskId());
            if (oldOpt.isEmpty()) {
                changes.add(new PlanChange(n.taskId(), ChangeType.ADDED, null, n.crewId(),
                        null, n.startSlot(), ChangeReason.OTHER));
                continue;
            }
            Assignment o = oldOpt.get();
            boolean moved = o.startSlot() != n.startSlot();
            boolean reassigned = !o.crewId().equals(n.crewId());
            if (!moved && !reassigned) continue;   // nothing changed

            ChangeType type;
            ChangeReason reason;
            if (moved && reassigned) {
                type = ChangeType.MOVED_AND_REASSIGNED;
                reason = reassignReason(task, o, n, crew, state);
                if (reason == ChangeReason.OTHER) reason = moveReason(task, n, state.forecast());
            } else if (reassigned) {
                type = ChangeType.REASSIGNED;
                reason = reassignReason(task, o, n, crew, state);
            } else {
                type = ChangeType.MOVED;
                reason = moveReason(task, n, state.forecast());
            }
            changes.add(new PlanChange(n.taskId(), type, o.crewId(), n.crewId(),
                    o.startSlot(), n.startSlot(), reason));
        }

        // a task that is still in the task list but has no place in the new plan
        for (Assignment o : state.currentSchedule().assignments()) {
            if (tasks.containsKey(o.taskId()) && next.find(o.taskId()).isEmpty()) {
                changes.add(new PlanChange(o.taskId(), ChangeType.DROPPED, o.crewId(), null,
                        o.startSlot(), null, ChangeReason.DROPPED_NON_MANDATORY));
            }
        }
        return changes;
    }

    /** Why the executor changed: the old one is not NORMAL, or the task no longer fits his budget. */
    private static ChangeReason reassignReason(Task task, Assignment o, Assignment n,
                                               Map<String, CrewMember> crew, MissionState state) {
        CrewMember old = crew.get(o.crewId());
        if (old == null) return ChangeReason.OTHER;
        if (old.status() != HealthStatus.NORMAL) return ChangeReason.REASSIGNED_CREW_FATIGUE;
        if (state.doseTable().forTask(task, n.startSlot()) > old.remainingDoseBudgetMicroSv()) {
            return ChangeReason.REASSIGNED_DOSE_BUDGET;
        }
        return ChangeReason.OTHER;
    }

    /** Why the start moved: only for an EVA with a storm expected; everything else is OTHER. */
    private static ChangeReason moveReason(Task task, Assignment n, SpaceWeatherForecast forecast) {
        if (task.type() == TaskType.EVA && forecast.stormExpected()) {
            if (n.startSlot() >= forecast.peakSlot()) return ChangeReason.MOVED_AFTER_STORM_PEAK;
            if (n.startSlot() + task.durationSlots() <= forecast.peakSlot()) {
                return ChangeReason.MOVED_BEFORE_STORM_PEAK;
            }
        }
        return ChangeReason.OTHER;
    }
}

