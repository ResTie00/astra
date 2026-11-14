package kg.astra.replan;

import kg.astra.common.SseHub;
import kg.astra.common.StreamEvent;
import kg.astra.common.StreamEventType;
import kg.astra.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReplanOrchestratorService {

    private final MissionStateAssembler assembler;
    private final ScenarioSampler sampler;
    private final RiskEvaluator riskEvaluator;
    private final Replanner replanner;                 // CP-SAT from DomainConfig
    private final ReplanRunRepository runRepository;
    private final SseHub sseHub;
    private final JsonMapper jsonMapper; // Jackson 3: tools.jackson.databind.json.JsonMapper
    private final ReplanResponseMapper mapper;
    private final ScheduleEntryRepository scheduleRepository;

    private final ExecutorService solverExecutor;      // see note below

    @Value("${astra.replan.monte-carlo-runs}")
    private int runs;

    public ReplanResponse replan(String reason) {
        Instant horizonStart = Instant.now().truncatedTo(ChronoUnit.HOURS);

        // 1. state from DB (short reads, no long transaction)
        MissionState state = assembler.assemble(horizonStart);

        // 2. scenarios ONCE: the same list for every plan
        List<SpeScenario> scenarios = sampler.sample(state.forecast(), runs, state.horizon().seed());

        // 3. risk of the current plan
        RiskReport currentRisk = riskEvaluator.evaluate(state.currentSchedule(), state, scenarios);

        // 4. CP-SAT on its own thread pool, with a hard timeout
        ReplanResult result = solve(state);

        // 5. risk of every option, on the same scenarios
        List<RiskReport> optionRisks = result.options().stream()
                .map(o -> riskEvaluator.evaluate(o.schedule(), state, scenarios))
                .toList();

        // 6. domain -> API (Mansur's ReplanResponseMapper: slots -> dates, µSv -> mSv, codes -> text)
        String runId = "run-" + UUID.randomUUID().toString().substring(0, 8);
        ReplanResponse response = mapper.toResponse(runId, state, horizonStart, currentRisk,
                result.options(), optionRisks);

        // 7. save + notify
        runRepository.save(new ReplanRun(runId, reason, horizonStart,
                state.horizon().slots(), state.horizon().seed(), jsonMapper.writeValueAsString(response)));
        sseHub.publish(StreamEvent.of(StreamEventType.REPLAN_READY, response));
        return response;
    }

    private ReplanResult solve(MissionState state) {
        Future<ReplanResult> future = solverExecutor.submit(() -> replanner.replan(state));
        try {
            long limitMs = (long) (state.horizon().solverTimeLimitSeconds() * 1000) + 2000;
            return future.get(limitMs, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            future.cancel(true);
            log.warn("Solver failed or timed out: {}", e.toString());
            return ReplanResult.noSolution(SolverStatus.UNKNOWN, 0);
        }

    }
    @Transactional
    public AcceptPlanResponse accept(String runId, String optionId) {
        ReplanRun run = runRepository.findById(runId)
                .orElseThrow(() -> new NoSuchElementException("Unknown replan run: " + runId));

        if (run.getAcceptedOptionId() != null) {
            throw new IllegalStateException("Run " + runId + " already accepted: " + run.getAcceptedOptionId());
        }

        // the options were stored as JSON when the run was created: no solver call here
        ReplanResponse response = jsonMapper.readValue(run.getResponse(), ReplanResponse.class);
        PlanOptionDto option = response.options().stream()
                .filter(o -> o.optionId().equals(optionId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Unknown option " + optionId + " in run " + runId));

        // replace the schedule only for tasks of this run: the current plan + the chosen plan
        // (a dropped task is in the current plan but not in the option, so its entry goes away too)
        Set<String> taskIds = new HashSet<>();
        response.currentPlan().assignments().forEach(a -> taskIds.add(a.taskId()));
        option.plan().assignments().forEach(a -> taskIds.add(a.taskId()));

        scheduleRepository.deleteByTaskIdIn(taskIds);
        scheduleRepository.flush();   // see note below

        scheduleRepository.saveAll(option.plan().assignments().stream()
                .map(a -> new ScheduleEntry(a.taskId(), a.crewId(), a.start(), a.end()))
                .toList());

        run.accept(optionId);
        runRepository.save(run);

        return new AcceptPlanResponse(runId, optionId, option.plan());
    }
}
