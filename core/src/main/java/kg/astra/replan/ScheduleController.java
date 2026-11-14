package kg.astra.replan;

import kg.astra.crew.Crew;
import kg.astra.crew.CrewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
class ScheduleController {

    private final ScheduleEntryRepository scheduleRepository;
    private final MissionTaskRepository taskRepository;
    private final CrewRepository crewRepository;

    /** Current accepted plan for the next 72 hours. */
    @GetMapping("/schedule")
    PlanDto schedule() {
        Instant from = Instant.now().truncatedTo(ChronoUnit.HOURS);
        Instant to = from.plus(72, ChronoUnit.HOURS);
        Map<String, MissionTask> tasks = taskRepository.findAll().stream()
                .collect(Collectors.toMap(MissionTask::getId, t -> t));
        Map<String, String> names = crewRepository.findAll().stream()
                .collect(Collectors.toMap(Crew::getId, Crew::getName));

        List<AssignmentDto> items = scheduleRepository
                .findByStartAtLessThanAndEndAtGreaterThanOrderByStartAt(to, from).stream()
                .filter(e -> tasks.containsKey(e.getTaskId()))
                .map(e -> {
                    MissionTask t = tasks.get(e.getTaskId());
                    return new AssignmentDto(t.getId(), t.getName(), t.getType(),
                            e.getCrewId(), names.get(e.getCrewId()), e.getStartAt(), e.getEndAt());
                })
                .toList();
        return new PlanDto(items);
    }
}
