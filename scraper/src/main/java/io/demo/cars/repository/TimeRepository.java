package io.demo.cars.repository;

import io.demo.cars.model.ScraperMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeRepository extends JpaRepository<ScraperMetadata, Long> {
}
