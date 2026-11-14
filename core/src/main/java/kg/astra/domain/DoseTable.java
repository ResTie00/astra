package kg.astra.domain;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

// expected dose in µSv for every task type in every hour of the plan.
// Core builds it once and gives it to the solver, so the solver doesn't need the dose model code.
// whole numbers only, because CP-SAT works with integers
public record DoseTable(Map<TaskType, long[]> microSvPerSlot) {

    public DoseTable {
        Objects.requireNonNull(microSvPerSlot, "microSvPerSlot");
        // copy the arrays: a record should not change if someone edits the original arrays later
        Map<TaskType, long[]> copy = new EnumMap<>(TaskType.class);
        microSvPerSlot.forEach((type, values) -> copy.put(type, values.clone()));
        // every task type needs a row, otherwise at() would crash later in the middle of solving
        for (TaskType t : TaskType.values()) {
            if (!copy.containsKey(t)) throw new IllegalArgumentException("missing dose row for " + t);
        }
        microSvPerSlot = Map.copyOf(copy);
    }

    // asks the dose model once for every task type and every hour
    public static DoseTable build(DoseModel model, SpaceWeatherForecast forecast, int slots) {
        Map<TaskType, long[]> rows = new EnumMap<>(TaskType.class);
        for (TaskType t : TaskType.values()) {
            long[] row = new long[slots];
            for (int s = 0; s < slots; s++) row[s] = model.expectedDoseMicroSv(t, s, forecast);
            rows.put(t, row);
        }
        return new DoseTable(rows);
    }

    // all rows have the same length, INTERNAL is just one that always exists
    public int slots() {
        return microSvPerSlot.get(TaskType.INTERNAL).length;
    }

    // dose of one hour of this task type
    public long at(TaskType type, int slot) {
        return microSvPerSlot.get(type)[slot];
    }

    // total dose of a task if it starts at startSlot: just add up its hours
    public long forTask(Task task, int startSlot) {
        long sum = 0;
        for (int s = startSlot; s < startSlot + task.durationSlots(); s++) sum += at(task.type(), s);
        return sum;
    }
}