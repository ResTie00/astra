package kg.astra.replan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/** One replanning run. The full ReplanResponse is stored as JSON so /accept can apply the chosen option. */
@Entity
@Table(name = "replan_run")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReplanRun {

    @Id
    private String id;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    private String reason;

    @Column(name = "horizon_start", nullable = false)
    private Instant horizonStart;

    @Column(name = "horizon_hours", nullable = false)
    private int horizonHours;

    @Column(nullable = false)
    private long seed;

    /** JSON text of ReplanResponse. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private String response;

    @Column(name = "accepted_option_id")
    private String acceptedOptionId;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    public ReplanRun(String id, String reason, Instant horizonStart, int horizonHours, long seed, String responseJson) {
        this.id = id;
        this.createdAt = Instant.now();
        this.reason = reason;
        this.horizonStart = horizonStart;
        this.horizonHours = horizonHours;
        this.seed = seed;
        this.response = responseJson;
    }

    public void accept(String optionId) {
        this.acceptedOptionId = optionId;
        this.acceptedAt = Instant.now();
    }
}
