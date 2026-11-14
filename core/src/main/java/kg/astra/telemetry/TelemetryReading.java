package kg.astra.telemetry;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * One metric value of one crew member at one moment (narrow form).
 * crewId is a plain column, not a @ManyToOne: no joins, fast batch inserts.
 */
@Entity
@Table(name = "telemetry_reading")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TelemetryReading {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "telemetry_seq")
    @SequenceGenerator(name = "telemetry_seq", sequenceName = "telemetry_seq", allocationSize = 50)
    private Long id;

    @Column(name = "crew_id", nullable = false)
    private String crewId;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Metric metric;

    @Column(nullable = false)
    private double value;

    public TelemetryReading(String crewId, Instant recordedAt, Metric metric, double value) {
        this.crewId = crewId;
        this.recordedAt = recordedAt;
        this.metric = metric;
        this.value = value;
    }

    public static TelemetryReading from(TelemetryReadingDto dto) {
        return new TelemetryReading(dto.crewId(), dto.timestamp(), dto.metric(), dto.value());
    }
}
