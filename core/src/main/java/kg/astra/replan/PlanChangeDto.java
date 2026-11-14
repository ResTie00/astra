package kg.astra.replan;

import kg.astra.domain.ChangeReason;
import kg.astra.domain.ChangeType;

import java.time.Instant;

//explanation is human text built by Core from the reason code
public record PlanChangeDto(String taskId, String taskName, ChangeType type,
                            String fromCrewId, String toCrewId, Instant fromStart, Instant toStart,
                            ChangeReason reason, String explanation) {}
