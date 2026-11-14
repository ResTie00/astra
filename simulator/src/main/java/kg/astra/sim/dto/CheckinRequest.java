package kg.astra.sim.dto;

import java.time.Instant;
import java.util.List;

/** POST /api/checkin. Simulator sends past days (explicit timestamp) to build the "quiet decline" history. */
public record CheckinRequest(String crewId, Instant timestamp, int wellbeing, int mood, int stress,
                             List<String> symptoms, Integer reactionMeanMs, Integer reactionLapses) {}
