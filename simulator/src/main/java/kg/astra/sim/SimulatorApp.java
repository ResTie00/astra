package kg.astra.sim;

import kg.astra.sim.dto.CheckinRequest;
import kg.astra.sim.dto.EnvironmentBatchRequest;
import kg.astra.sim.dto.EnvironmentReadingDto;
import kg.astra.sim.dto.SpaceWeatherEventRequest;
import kg.astra.sim.dto.TelemetryBatchRequest;
import kg.astra.sim.dto.TelemetryReadingDto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Astra telemetry simulator.
 *
 * 1) History: sends the last N days (hourly vitals, daily sleep and check-ins), so every
 *    crew member has a personal baseline. One of them is in a "quiet decline" for the last 3 days.
 * 2) Live: sends current readings every few seconds.
 * 3) Console: type "storm" + Enter to send a solar particle event forecast, "quit" to stop.
 *
 * Options: --url=http://localhost:8080 --days=14 --interval=5 --seed=42
 *          --decline=c2 --skip-history --no-live --storm-in-hours=20 --flux=1000
 */
public class SimulatorApp {

    private static final int BATCH = 500;
    private static final int DECLINE_DAYS = 3;

    public static void main(String[] args) throws Exception {
        Map<String, String> opt = parse(args);
        String url = opt.getOrDefault("url", "http://localhost:8080");
        int days = Integer.parseInt(opt.getOrDefault("days", "14"));
        int interval = Integer.parseInt(opt.getOrDefault("interval", "5"));
        long seed = Long.parseLong(opt.getOrDefault("seed", "42"));
        String declineCrew = opt.getOrDefault("decline", "c2");
        int stormInHours = Integer.parseInt(opt.getOrDefault("storm-in-hours", "20"));
        double flux = Double.parseDouble(opt.getOrDefault("flux", "1000"));

        Instant now = Instant.now().truncatedTo(ChronoUnit.HOURS);
        Instant today = now.truncatedTo(ChronoUnit.DAYS);
        Scenario scenario = new Scenario(declineCrew, today.minus(DECLINE_DAYS - 1, ChronoUnit.DAYS));
        TelemetryGenerator generator = new TelemetryGenerator(scenario, seed);
        List<CrewProfile> crew = CrewProfile.crew();
        CoreClient core = new CoreClient(url);

        System.out.println("Waiting for Core at " + url + " ...");
        if (!core.waitUntilUp(60)) {
            System.err.println("Core is not reachable. Start it first.");
            System.exit(1);
        }

        if (!opt.containsKey("skip-history")) {
            sendHistory(core, generator, crew, today.minus(days, ChronoUnit.DAYS), now);
            writeLabels(scenario, now);
        }
        if (opt.containsKey("no-live")) return;

        startConsole(core, stormInHours, flux);
        System.out.println("Live mode: every " + interval + " s. Type 'storm' + Enter to send a storm forecast, 'quit' to stop.");
        while (true) {
            Instant t = Instant.now().truncatedTo(ChronoUnit.SECONDS);
            List<TelemetryReadingDto> vitals = new ArrayList<>();
            for (CrewProfile p : crew) vitals.addAll(generator.vitals(p, t));
            try {
                core.post("/api/telemetry", new TelemetryBatchRequest(vitals));
                core.post("/api/environment", new EnvironmentBatchRequest(generator.environment(t)));
            } catch (IOException e) {
                System.err.println("Live send failed: " + e.getMessage());   // keep going, Core may be restarting
            }
            Thread.sleep(interval * 1000L);
        }
    }

    /** Hourly vitals and environment, plus sleep (06:00) and check-in (07:00) once per day. */
    private static void sendHistory(CoreClient core, TelemetryGenerator generator, List<CrewProfile> crew,
                                    Instant from, Instant to) throws IOException, InterruptedException {
        List<TelemetryReadingDto> vitals = new ArrayList<>();
        List<EnvironmentReadingDto> environment = new ArrayList<>();
        List<CheckinRequest> checkins = new ArrayList<>();

        for (Instant t = from; t.isBefore(to); t = t.plus(1, ChronoUnit.HOURS)) {
            int hour = (int) (Duration.between(t.truncatedTo(ChronoUnit.DAYS), t).toHours());
            for (CrewProfile p : crew) {
                vitals.addAll(generator.vitals(p, t));
                if (hour == 6) vitals.add(generator.sleep(p, t));
                if (hour == 7) checkins.add(generator.checkin(p, t));
            }
            environment.addAll(generator.environment(t));
        }

        for (int i = 0; i < vitals.size(); i += BATCH) {
            core.post("/api/telemetry", new TelemetryBatchRequest(vitals.subList(i, Math.min(i + BATCH, vitals.size()))));
        }
        for (int i = 0; i < environment.size(); i += BATCH) {
            core.post("/api/environment",
                    new EnvironmentBatchRequest(environment.subList(i, Math.min(i + BATCH, environment.size()))));
        }
        for (CheckinRequest c : checkins) core.post("/api/checkin", c);

        System.out.printf("History sent: %d telemetry readings, %d environment readings, %d check-ins (%s .. %s)%n",
                vitals.size(), environment.size(), checkins.size(), from, to);
    }

    /** Ground truth of what was injected. Stays on disk; never sent to Core. */
    private static void writeLabels(Scenario scenario, Instant now) throws IOException {
        String csv = "crewId,type,start,end\n"
                + scenario.declineCrewId() + ",QUIET_DECLINE," + scenario.declineStart() + "," + now + "\n";
        Files.writeString(Path.of("labels.csv"), csv);
        System.out.println("Injected: QUIET_DECLINE for " + scenario.declineCrewId()
                + " since " + scenario.declineStart() + " (labels.csv)");
    }

    /** Reads commands from the keyboard in a background thread, so the presenter controls the demo. */
    private static void startConsole(CoreClient core, int stormInHours, double flux) {
        Thread console = new Thread(() -> {
            Scanner in = new Scanner(System.in);
            while (in.hasNextLine()) {
                String command = in.nextLine().trim().toLowerCase();
                if (command.equals("quit")) System.exit(0);
                if (command.equals("storm")) {
                    Instant onset = Instant.now().truncatedTo(ChronoUnit.HOURS).plus(stormInHours, ChronoUnit.HOURS);
                    var event = new SpaceWeatherEventRequest("SPE", onset, onset.plus(6, ChronoUnit.HOURS), flux, 18);
                    try {
                        core.post("/api/space-weather", event);
                        System.out.println("Storm forecast sent: onset " + onset + ", peak flux " + flux + " pfu");
                    } catch (Exception e) {
                        System.err.println("Storm send failed: " + e.getMessage());
                    }
                }
            }
        });
        console.setDaemon(true);
        console.start();
    }

    /** "--key=value" and "--flag" into a map. */
    private static Map<String, String> parse(String[] args) {
        Map<String, String> opt = new HashMap<>();
        for (String a : args) {
            if (!a.startsWith("--")) continue;
            int eq = a.indexOf('=');
            if (eq < 0) opt.put(a.substring(2), "true");
            else opt.put(a.substring(2, eq), a.substring(eq + 1));
        }
        return opt;
    }
}
