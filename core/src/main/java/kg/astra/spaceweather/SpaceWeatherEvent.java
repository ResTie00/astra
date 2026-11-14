package kg.astra.spaceweather;

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

@Entity
@Table(name = "space_weather_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpaceWeatherEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private Instant onset;

    @Column(nullable = false)
    private Instant peak;

    @Column(name = "peak_proton_flux_pfu", nullable = false)
    private double peakProtonFluxPfu;

    @Column(name = "duration_hours", nullable = false)
    private int durationHours;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    public static SpaceWeatherEvent from(SpaceWeatherEventRequest r) {
        SpaceWeatherEvent e = new SpaceWeatherEvent();
        e.type = r.type();
        e.onset = r.onset();
        e.peak = r.peak();
        e.peakProtonFluxPfu = r.peakProtonFluxPfu();
        e.durationHours = r.durationHours();
        e.receivedAt = Instant.now();
        return e;
    }

    public Instant end() {
        return onset.plusSeconds(durationHours * 3600L);
    }
}
