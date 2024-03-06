package io.demo.cars.orchestration;

import io.demo.cars.service.ScraperService;
import io.demo.cars.utils.ArgumentParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor(staticName = "of")
public class ServiceNode {

    private final ScraperService service;

    public boolean shouldExecute(List<String> args) {
        return args.stream()
            .noneMatch(arg -> ArgumentParser.getArgumentToSkipableServiceMap().containsKey(arg)
                && ArgumentParser.getArgumentToSkipableServiceMap().get(arg).equals(this.service.getClass()));
    }

    public void doExecute() {
        log.info("Execution of scraper service with name: {} started", this.service.getClass().getSimpleName());
        service.scrape();
    }

    public String getServiceName() {
        return this.service.getClass().getName();
    }
}
