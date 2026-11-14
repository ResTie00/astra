package kg.astra.domain;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Expected dose (integer µSv) for each task type in each slot.
 * Built by Core from the DoseModel and handed to the solver, so the solver
 * does not depend on the dose model code and works with integers only (CP-SAT requirement).
 */
public record DoseTable(Map<TaskType, long[]> microSvPerSlot) {

    public DoseTable {
        Objects.requireNonNull(microSvPerSlot, "microSvPerSlot");
        Map<TaskType, long[]> copy = new EnumMap<>(TaskType.class);
        microSvPerSlot.forEach((type, values) -> copy.put(type, values.clone()));
        for (TaskType t : TaskType.values()) {
            if (!copy.containsKey(t)) throw new IllegalArgumentException("missing dose row for " + t);
        }
        microSvPerSlot = Map.copyOf(copy);
    }

    /** Builds the table by asking the dose model for every (type, slot). */
    public static DoseTable build(DoseModel model, SpaceWeatherForecast forecast, int slots) {
        Map<TaskType, long[]> rows = new EnumMap<>(TaskType.class);
        for (TaskType t : TaskType.values()) {
            long[] row = new long[slots];
            for (int s = 0; s < slots; s++) row[s] = model.expectedDoseMicroSv(t, s, forecast);
            rows.put(t, row);
        }
        return new DoseTable(rows);
    }

    public int slots() {
        return microSvPerSlot.get(TaskType.INTERNAL).length;
    }

    /** Dose of one slot of this task type. */
    public long at(TaskType type, int slot) {
        return microSvPerSlot.get(type)[slot];
    }

    /** Total dose of doing this task starting at startSlot. */
    public long forTask(Task task, int startSlot) {
        long sum = 0;
        for (int s = startSlot; s < startSlot + task.durationSlots(); s++) sum += at(task.type(), s);
        return sum;
    }
}
