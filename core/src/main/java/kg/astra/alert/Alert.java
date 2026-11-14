package kg.astra.alert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "alert")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** null = mission-wide alert. */
    @Column(name = "crew_id")
    private String crewId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertSeverity severity;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String message;

    /** JSON text with the data behind the alert (values, thresholds). */
    @JdbcTypeCode(SqlTypes.JSON)
    private String evidence;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private boolean acknowledged;

    public Alert(String crewId, AlertSeverity severity, String type, String message, String evidenceJson) {
        this.crewId = crewId;
        this.severity = severity;
        this.type = type;
        this.message = message;
        this.evidence = evidenceJson;
        this.createdAt = Instant.now();
    }

    public void acknowledge() {
        this.acknowledged = true;
    }

    public AlertDto toDto() {
        return new AlertDto(id, crewId, severity, type, message, createdAt);
    }
}
