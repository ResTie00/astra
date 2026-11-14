package kg.astra.telemetry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface EnvironmentRepository extends JpaRepository<EnvironmentReading, Long> {

    /** Latest value of every (module, metric) pair. */
    @Query(value = """
            SELECT DISTINCT ON (module, metric) *
            FROM environment_reading
            ORDER BY module, metric, recorded_at DESC
            """, nativeQuery = true)
    List<EnvironmentReading> findLatestPerModuleAndMetric();

    List<EnvironmentReading> findByMetricAndRecordedAtAfterOrderByRecordedAt(EnvMetric metric, Instant after);
}
