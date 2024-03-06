package io.demo.cars.orchestration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@RequiredArgsConstructor(staticName = "of")
public class Orchestrator {

    private final List<ServiceNode> serviceNodes;
    private final List<String> args;

    public void execute() {
        log.info("Iteration over Scraper Services has started current number of services in list is: {} ", serviceNodes.size());
        for (ServiceNode serviceNode : serviceNodes) {
            LocalDateTime startTime = LocalDateTime.now();

            if (serviceNode.shouldExecute(args)) {
                serviceNode.doExecute();
                log.info("Total execution time for {} took {} seconds.", serviceNode.getServiceName(),
                    ChronoUnit.SECONDS.between(startTime, LocalDateTime.now()));
            } else {
                log.info("Skipping run for: {}", serviceNode.getServiceName());
            }
        }
    }
}
