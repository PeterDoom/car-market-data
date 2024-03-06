package io.demo.cars.service.impl;

import io.demo.cars.model.ScraperMetadata;
import io.demo.cars.repository.TimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TimeService {
    private final TimeRepository timeRepository;

    public void handleMetadata(Long startedAt, Long endedAt) {
        timeRepository.save(ScraperMetadata.builder()
            .startedAt(startedAt)
            .endedAt(endedAt)
            .build());
    }
}
