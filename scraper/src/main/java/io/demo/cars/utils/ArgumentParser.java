package io.demo.cars.utils;

import io.demo.cars.service.impl.AdvertService;
import io.demo.cars.service.impl.CarService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ArgumentParser {

    private static final Map<String, Object> argumentToSkipableServiceMap = Map.of(
        "skip-car-brands", CarService.class,
        "skip-adverts", AdvertService.class
    );

    public static Map<String, Object> getArgumentToSkipableServiceMap() {
        return argumentToSkipableServiceMap;
    }
}
