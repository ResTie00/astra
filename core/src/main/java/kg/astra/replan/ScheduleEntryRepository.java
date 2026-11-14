package kg.astra.replan;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface ScheduleEntryRepository extends JpaRepository<ScheduleEntry, Long> {

    /** Entries overlapping [from, to): start < to AND end > from. */
    List<ScheduleEntry> findByStartAtLessThanAndEndAtGreaterThanOrderByStartAt(Instant to, Instant from);

    List<ScheduleEntry> findByCrewIdOrderByStartAt(String crewId);
}
