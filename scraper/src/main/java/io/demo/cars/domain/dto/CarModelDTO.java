package io.demo.cars.domain.dto;

import io.demo.cars.model.CarBrand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarModelDTO {
    private String brandName;
    private CarBrand brand;
}
