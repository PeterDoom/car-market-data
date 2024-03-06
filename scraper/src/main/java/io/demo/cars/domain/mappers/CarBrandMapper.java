package io.demo.cars.domain.mappers;

import io.demo.cars.domain.dto.CarBrandDTO;
import io.demo.cars.model.CarBrand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CarBrandMapper {

    CarBrandDTO toCarBrandDTO(CarBrand carBrand);

    List<CarBrandDTO> toCarBrandDTOList(List<CarBrand> carBrandList);

    @Mapping(source = "brandName", target = "brandName")
    CarBrand toCarBrand(CarBrandDTO carBrandDTO);

}
