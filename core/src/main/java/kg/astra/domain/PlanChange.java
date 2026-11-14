package kg.astra.domain;

/**
 * One difference between the current schedule and a proposed one.
 * Slot fields are null where they do not apply (e.g. toStartSlot for DROPPED).
 */
public record PlanChange(String taskId, ChangeType type, String fromCrewId, String toCrewId,
                         Integer fromStartSlot, Integer toStartSlot, ChangeReason reason) {}
