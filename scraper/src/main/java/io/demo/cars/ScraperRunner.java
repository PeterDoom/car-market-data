package io.demo.cars;

import io.demo.cars.orchestration.Orchestrator;
import io.demo.cars.orchestration.ServiceNode;
import io.demo.cars.service.impl.AdvertService;
import io.demo.cars.service.impl.CarService;
import io.demo.cars.service.impl.FilterFormService;
import io.demo.cars.service.impl.TimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScraperRunner implements CommandLineRunner {
    private final CarService carService;
    private final AdvertService advertService;
    private final FilterFormService filterFormService;
    private final TimeService timeService;

    @Override
    public void run(String[] args) {
        Long startTime = Instant.now().toEpochMilli();
        final List<ServiceNode> serviceNodeList = List.of(
            ServiceNode.of(carService),
            ServiceNode.of(filterFormService),
            ServiceNode.of(advertService)
        );
        Orchestrator orchestrator = Orchestrator.of(serviceNodeList, Arrays.asList(args));
        orchestrator.execute();
        Long endTime = Instant.now().toEpochMilli();
        timeService.handleMetadata(startTime, endTime);
        log.info("Total time for scraping: {} ms.", endTime - startTime);
    }
}

