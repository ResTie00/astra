package kg.astra.replan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

//One assignment of the current (accepted) schedule
@Entity
@Table(name = "schedule_entry")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false, unique = true)
    private String taskId;

    @Column(name = "crew_id", nullable = false)
    private String crewId;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    public ScheduleEntry(String taskId, String crewId, Instant startAt, Instant endAt) {
        this.taskId = taskId;
        this.crewId = crewId;
        this.startAt = startAt;
        this.endAt = endAt;
    }
}
