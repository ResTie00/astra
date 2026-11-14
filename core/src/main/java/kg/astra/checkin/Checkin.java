package kg.astra.checkin;

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
import java.util.Arrays;
import java.util.List;

/** Self-report + optional reaction test result. */
@Entity
@Table(name = "checkin")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Checkin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "crew_id", nullable = false)
    private String crewId;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(nullable = false)
    private int wellbeing;

    @Column(nullable = false)
    private int mood;

    @Column(nullable = false)
    private int stress;

    /** Comma-separated in the DB. */
    @Column(nullable = false)
    private String symptoms;

    @Column(name = "reaction_mean_ms")
    private Integer reactionMeanMs;

    @Column(name = "reaction_lapses")
    private Integer reactionLapses;

    /** Builds an entity from the API request. Null timestamp = now (UI); simulator sends past times. */
    public static Checkin from(CheckinRequest r) {
        Checkin c = new Checkin();
        c.crewId = r.crewId();
        c.recordedAt = r.timestamp() != null ? r.timestamp() : Instant.now();
        c.wellbeing = r.wellbeing();
        c.mood = r.mood();
        c.stress = r.stress();
        c.symptoms = String.join(",", r.symptoms());
        c.reactionMeanMs = r.reactionMeanMs();
        c.reactionLapses = r.reactionLapses();
        return c;
    }

    public List<String> symptomList() {
        if (symptoms == null || symptoms.isBlank()) return List.of();
        return Arrays.stream(symptoms.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }
}
