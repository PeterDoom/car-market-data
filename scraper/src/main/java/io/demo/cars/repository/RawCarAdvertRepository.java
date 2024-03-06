package io.demo.cars.repository;

import io.demo.cars.model.RawCarAdvert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RawCarAdvertRepository extends JpaRepository<RawCarAdvert, Long> {
}
