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

@Entity
@Table(name = "environment_reading")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EnvironmentReading {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "environment_seq")
    @SequenceGenerator(name = "environment_seq", sequenceName = "environment_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false)
    private String module;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnvMetric metric;

    @Column(nullable = false)
    private double value;

    public EnvironmentReading(String module, Instant recordedAt, EnvMetric metric, double value) {
        this.module = module;
        this.recordedAt = recordedAt;
        this.metric = metric;
        this.value = value;
    }

    public static EnvironmentReading from(EnvironmentReadingDto dto) {
        return new EnvironmentReading(dto.module(), dto.timestamp(), dto.metric(), dto.value());
    }
}
