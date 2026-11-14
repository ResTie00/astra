package kg.astra.sim;

import com.fasterxml.jackson.databind.ObjectMapper;
import kg.astra.sim.dto.JsonSetup;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Thin HTTP client for Core. */
public class CoreClient {

    private final String baseUrl;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    private final ObjectMapper json = JsonSetup.mapper();

    public CoreClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    /** POSTs the body as JSON. Throws if Core answers with a non-2xx status. */
    public void post(String path, Object body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)))
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IOException("POST " + path + " -> " + response.statusCode() + " " + response.body());
        }
    }

    /** Waits until Core answers on /actuator/health. */
    public boolean waitUntilUp(int seconds) throws InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/actuator/health"))
                .timeout(Duration.ofSeconds(2)).GET().build();
        for (int i = 0; i < seconds; i++) {
            try {
                if (http.send(request, HttpResponse.BodyHandlers.discarding()).statusCode() == 200) return true;
            } catch (IOException e) {
                // not up yet
            }
            Thread.sleep(1000);
        }
        return false;
    }
}
