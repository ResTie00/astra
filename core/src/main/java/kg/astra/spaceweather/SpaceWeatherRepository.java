package kg.astra.spaceweather;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpaceWeatherRepository extends JpaRepository<SpaceWeatherEvent, Long> {

    /** The most recently received event is the current forecast. */
    Optional<SpaceWeatherEvent> findTopByOrderByReceivedAtDesc();
}
