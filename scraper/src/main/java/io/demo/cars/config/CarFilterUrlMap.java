package io.demo.cars.config;

import io.demo.cars.model.CarModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class CarFilterUrlMap {
    Map<CarModel, String> urlHolderMap = new ConcurrentHashMap<>();

    @Bean
    public Map<CarModel, String> getCarFilterUrlMap() {
        return urlHolderMap;
    }
}
