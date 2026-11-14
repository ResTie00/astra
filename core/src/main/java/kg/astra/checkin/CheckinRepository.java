package kg.astra.checkin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CheckinRepository extends JpaRepository<Checkin, Long> {

    Optional<Checkin> findTopByCrewIdOrderByRecordedAtDesc(String crewId);

    List<Checkin> findByCrewIdAndRecordedAtAfterOrderByRecordedAt(String crewId, Instant after);

    List<Checkin> findByCrewIdAndRecordedAtBetweenOrderByRecordedAt(String crewId, Instant before, Instant after);
}
