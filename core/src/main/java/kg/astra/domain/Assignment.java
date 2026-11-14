package kg.astra.domain;

/** "This crew member does this task starting at this slot." */
public record Assignment(String taskId, String crewId, int startSlot) {}
