package io.demo.cars.domain.mappers;

import io.demo.cars.domain.dto.CarModelDTO;
import io.demo.cars.model.CarModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CarModelMapper {

    CarModelDTO toCarModelDTO(CarModel carModel);

    @Mapping(source = "brand", target = "brand")
    @Mapping(source = "brandName", target = "brandName")
    CarModel toCarModel(CarModelDTO carModelDTO);

    List<CarModelDTO> toCarModelDTOList(List<CarModel> carModelList);
}
