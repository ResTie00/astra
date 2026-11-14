package kg.astra.config;

import com.google.ortools.Loader;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.IntVar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class SolverConfig {

    private static final Logger log = LoggerFactory.getLogger(SolverConfig.class);

    //Loads OR-Tools native libraries and solves a tiny model at startup, so the first real call is not slow
    @Bean
    public ApplicationRunner ortoolsWarmUp() {
        return args -> {
            Loader.loadNativeLibraries();
            CpModel model = new CpModel();
            IntVar x = model.newIntVar(0, 10, "x");
            model.maximize(x);
            CpSolverStatus status = new CpSolver().solve(model);
            log.info("OR-Tools loaded, warm-up status: {}", status);
        };
    }

    //Solver and Monte Carlo are CPU-heavy: run them on their own small pool, not on request threads
    @Bean(destroyMethod = "shutdown")
    public ExecutorService solverExecutor() {
        return Executors.newFixedThreadPool(2);
    }
}
