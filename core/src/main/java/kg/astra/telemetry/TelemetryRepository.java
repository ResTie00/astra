package kg.astra.telemetry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface TelemetryRepository extends JpaRepository<TelemetryReading, Long> {

    /** History of one metric for the personal baseline. */
    List<TelemetryReading> findByCrewIdAndMetricAndRecordedAtAfterOrderByRecordedAt(
            String crewId, Metric metric, Instant after);

    /** Everything of one crew member in a window (status, blind spot, dose). */
    List<TelemetryReading> findByCrewIdAndRecordedAtBetweenOrderByRecordedAt(
            String crewId, Instant from, Instant to);

    /** Latest value of every metric for one crew member (dashboard). */
    @Query(value = """
            SELECT DISTINCT ON (metric) *
            FROM telemetry_reading
            WHERE crew_id = :crewId
            ORDER BY metric, recorded_at DESC
            """, nativeQuery = true)
    List<TelemetryReading> findLatestPerMetric(@Param("crewId") String crewId);
}
