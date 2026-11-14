package kg.astra.demo;

import kg.astra.domain.TaskType;
import kg.astra.replan.MissionTask;
import kg.astra.replan.MissionTaskRepository;
import kg.astra.replan.ScheduleEntry;
import kg.astra.replan.ScheduleEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

//Demo mission: tasks and a current schedule, relative to NOW (next full hour = hour 0).
//Loaded on startup if there are no tasks; POST /api/demo/reset reloads it (e.g. right before the demo).

//Story: Maya (c2) has the antenna repair EVA at hour +21. The simulator's "storm" command
//creates a storm from hour +20 for 18 h, so the EVA falls into the storm AND Maya is in a quiet decline.
//Windows are wide, so the solver can always move EVAs after the storm.
@Component
@RequiredArgsConstructor
@Slf4j
public class DemoDataLoader implements ApplicationRunner {

    private static final int HORIZON_HOURS = 72;

    private final MissionTaskRepository taskRepository;
    private final ScheduleEntryRepository scheduleRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (taskRepository.count() > 0) return;
        load();
    }

    @Transactional
    public void reload() {
        scheduleRepository.deleteAllInBatch();   // schedule first: it references tasks
        taskRepository.deleteAllInBatch();
        load();
    }

    private void load() {
        Instant h0 = Instant.now().truncatedTo(ChronoUnit.HOURS).plus(1, ChronoUnit.HOURS);
        Instant windowEnd = h0.plus(HORIZON_HOURS - 2, ChronoUnit.HOURS);

        //      id     name                         type               hours skill       mandatory  window from  crew  start
        task(h0, "t01", "Antenna repair (EVA)",       TaskType.EVA,      4, "EVA",      true,  2, windowEnd, "c2", 21);
        task(h0, "t02", "Solar panel inspection (EVA)", TaskType.EVA,    3, "EVA",      false, 24, windowEnd, "c4", 30);
        task(h0, "t03", "Life support check",         TaskType.INTERNAL, 2, null,       true,  0, windowEnd, "c1", 3);
        task(h0, "t04", "Medical check-up",           TaskType.INTERNAL, 1, "MEDIC",    true,  0, windowEnd, "c3", 5);
        task(h0, "t05", "Plant growth experiment",    TaskType.INTERNAL, 3, "SCIENCE",  true,  0, windowEnd, "c4", 6);
        task(h0, "t06", "Water recycler maintenance", TaskType.INTERNAL, 2, "ENGINEER", true,  0, windowEnd, "c2", 8);
        task(h0, "t07", "Navigation update",          TaskType.INTERNAL, 1, "PILOT",    true,  0, windowEnd, "c1", 15);
        task(h0, "t08", "Sample analysis",            TaskType.INTERNAL, 2, "SCIENCE",  false, 0, windowEnd, "c4", 26);

        log.info("Demo mission loaded: 8 tasks from {}", h0);
    }

    private void task(Instant h0, String id, String name, TaskType type, int hours, String skill,
                      boolean mandatory, int earliestHour, Instant latestEnd, String crewId, int startHour) {
        taskRepository.save(new MissionTask(id, name, type, hours, skill, mandatory,
                h0.plus(earliestHour, ChronoUnit.HOURS), latestEnd));
        Instant start = h0.plus(startHour, ChronoUnit.HOURS);
        scheduleRepository.save(new ScheduleEntry(id, crewId, start, start.plus(hours, ChronoUnit.HOURS)));
    }
}
