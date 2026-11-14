package kg.astra.domain;

import java.util.Objects;

/**
 * A unit of work to be scheduled.
 *
 * @param requiredSkill    null means anyone can do it
 * @param earliestStartSlot task cannot start before this slot
 * @param latestEndSlot    task must finish by this slot (exclusive end)
 */
public record Task(String id, String name, TaskType type, int durationSlots, String requiredSkill,
                   boolean mandatory, int earliestStartSlot, int latestEndSlot) {

    public Task {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(type, "type");
        if (durationSlots <= 0) throw new IllegalArgumentException("durationSlots must be > 0: " + id);
        if (earliestStartSlot < 0) throw new IllegalArgumentException("earliestStartSlot must be >= 0: " + id);
        if (latestEndSlot < earliestStartSlot + durationSlots)
            throw new IllegalArgumentException("task window too small: " + id);
    }
}
