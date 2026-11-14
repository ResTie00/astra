package kg.astra.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

// without this Spring ignores every @Scheduled method: HealthMonitor and the SSE ping would never run@Configuration
@EnableScheduling
public class SchedulingConfig {
}
