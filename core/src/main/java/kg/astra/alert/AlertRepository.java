package kg.astra.alert;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findTop50ByOrderByCreatedAtDesc();

    List<Alert> findByAcknowledgedFalseOrderByCreatedAtDesc();

    /** Anti-spam check for a crew member's alert. */
    boolean existsByCrewIdAndTypeAndCreatedAtAfter(String crewId, String type, Instant since);

    /** Anti-spam check for a mission-wide alert (crew_id IS NULL: "= null" would never match in SQL). */
    boolean existsByCrewIdIsNullAndTypeAndCreatedAtAfter(String type, Instant since);
}
