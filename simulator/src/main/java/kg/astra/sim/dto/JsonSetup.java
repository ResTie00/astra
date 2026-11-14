package kg.astra.sim.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Jackson must write Instant as ISO-8601 text ("2026-11-14T06:00:00Z"), not as a number.
 * Maven: jackson-databind + jackson-datatype-jsr310.
 */
public final class JsonSetup {
    private JsonSetup() {}

    public static ObjectMapper mapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
