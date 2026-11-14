package kg.astra.replan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kg.astra.domain.TaskType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "mission_task")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MissionTask {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskType type;

    @Column(name = "duration_hours", nullable = false)
    private int durationHours;

    //null = anyone can do it
    @Column(name = "required_skill")
    private String requiredSkill;

    @Column(nullable = false)
    private boolean mandatory;

    @Column(name = "earliest_start", nullable = false)
    private Instant earliestStart;

    @Column(name = "latest_end", nullable = false)
    private Instant latestEnd;
}
