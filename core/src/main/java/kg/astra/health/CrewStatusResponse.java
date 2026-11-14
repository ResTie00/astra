package kg.astra.health;

import kg.astra.domain.HealthStatus;

import java.time.Instant;
import java.util.List;

//GET /api/crew/{id}/status. Doses here are in mSv (display units)
public record CrewStatusResponse(
        String crewId,
        String name,
        Instant asOf,
        HealthStatus overall,
        List<AreaStatusDto> areas,
        BlindSpotDto blindSpot,
        RecommendedActionDto action,
        double cumulativeDoseMilliSv,
        double doseBudgetMilliSv) {}
