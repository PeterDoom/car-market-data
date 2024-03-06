package io.demo.cars.service.impl;

import com.gargoylesoftware.htmlunit.WebClient;
import io.demo.cars.builders.HtmlUnitWebClientBuilder;
import io.demo.cars.domain.dto.CarBrandDTO;
import io.demo.cars.domain.dto.CarModelDTO;
import io.demo.cars.domain.mappers.CarBrandMapper;
import io.demo.cars.domain.mappers.CarModelMapper;
import io.demo.cars.model.CarBrand;
import io.demo.cars.model.CarModel;
import io.demo.cars.repository.CarBrandRepository;
import io.demo.cars.repository.CarModelRepository;
import io.demo.cars.scrapper.ScrapperService;
import io.demo.cars.service.ScraperService;
import io.demo.cars.utils.PageProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

import static io.demo.cars.constant.Constants.MOBILE_BG_BASE_URL;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarService implements ScraperService {

    private final ScrapperService scrapperService;
    private final CarBrandRepository carBrandRepository;
    private final CarModelRepository carModelRepository;
    private final CarModelMapper carModelMapper;
    private final CarBrandMapper carBrandMapper;

    @Value("${exclusions.excluded-list}")
    private List<String> excluded;

    @Override
    public void scrape() {
        List<CarBrand> vehicleBrandsSaved = carBrandRepository.findAll();
        List<CarBrand> vehicleBrandsScraped = this.fetchBrands();

        log.info("Total number of brands found: {} ", vehicleBrandsScraped.size());

        if (shouldUpdateCarBrands(vehicleBrandsScraped, vehicleBrandsSaved)) {
            log.info("Extracted brands are more than inserted brands, updating DB");
            vehicleBrandsSaved = updateCarBrandDB(vehicleBrandsScraped, vehicleBrandsSaved);
        }

        vehicleBrandsSaved.forEach(carBrand -> {
            List<CarModel> carModelList = carModelRepository.findCarModelByBrandId(carBrand.getId());
            List<CarModel> modelsByBrand = fetchCarModelsByBrand(carBrand);

            if (shouldUpdateCarModels(modelsByBrand, carModelList)) {
                log.info("Extracted models by brand are {}, inserted models by brand are {}, updating DB", modelsByBrand.size(), carModelList.size());
                updateCardModelDB(modelsByBrand, carModelList);
            }
        });
    }

    private List<CarBrand> fetchBrands() {
        Document result = scrapperService.scrape(MOBILE_BG_BASE_URL);
        Elements elements = result.select("select[name=marka]");
        Elements scrapedBrands = elements.get(0).children();

        return scrapedBrands.stream()
            .map(x -> CarBrand.builder()
                .brandName(x.text())
                .build())
            .toList();
    }

    private List<CarModel> fetchCarModelsByBrand(CarBrand brand) {
        WebClient webClient = HtmlUnitWebClientBuilder.buildWebClient();

        List<String> vehicleModels = PageProcessor.getAllModelsByBrand(
            webClient,
            MOBILE_BG_BASE_URL,
            brand.getBrandName()
        );
        return vehicleModels.stream()
            .filter(s -> !s.equalsIgnoreCase("всички") &&
                !s.equalsIgnoreCase("други"))
            .filter(model -> !excluded.contains(String.format("%s:%s", brand.getBrandName(), model)))
            .map(x -> CarModel.builder()
                .brandName(x)
                .brand(brand)
                .build())
            .toList();
    }

    private List<CarBrand> updateCarBrandDB(List<CarBrand> carBrandsScraped, List<CarBrand> carBrandsFromDB) {
        List<CarBrandDTO> carBrandDTOListScraped = carBrandMapper.toCarBrandDTOList(carBrandsScraped);
        List<CarBrandDTO> carBrandDTOFromDB = carBrandMapper.toCarBrandDTOList(carBrandsFromDB);

        List<CarBrand> carBrandsToPersist = carBrandDTOListScraped.stream()
            .filter(x -> !carBrandDTOFromDB.contains(x))
            .filter(x -> !x.getBrandName().equals("всички"))
            .map(carBrandMapper::toCarBrand)
            .toList();
        return carBrandRepository.saveAllAndFlush(carBrandsToPersist);
    }

    private void updateCardModelDB(List<CarModel> carModelScraped, List<CarModel> carModelFromDB) {
        if (carModelScraped.size() > carModelFromDB.size()) {
            List<CarModelDTO> carModelDTOListScraped = carModelMapper.toCarModelDTOList(carModelScraped);
            List<CarModelDTO> carModelDTOFromDB = carModelMapper.toCarModelDTOList(carModelFromDB);

            List<CarModel> carBrandsToPersist = carModelDTOListScraped.stream()
                .filter(x -> !carModelDTOFromDB.contains(x))
                .map(carModelMapper::toCarModel)
                .toList();
            carModelRepository.saveAllAndFlush(carBrandsToPersist);
        }
    }

    private boolean shouldUpdateCarBrands(List<CarBrand> carBrandsScraped, List<CarBrand> carBrandsFromDB) {
        return carBrandsScraped.size() != carBrandsFromDB.size()
            && new HashSet<>(
            carBrandsScraped.stream().map(carBrandMapper::toCarBrandDTO).toList()
        ).containsAll(
            carBrandsFromDB.stream().map(carBrandMapper::toCarBrandDTO).toList()
        );
    }

    private boolean shouldUpdateCarModels(List<CarModel> carModelsScraped, List<CarModel> carModelsFromDB) {
        return carModelsScraped.size() != carModelsFromDB.size()
            && new HashSet<>(
            carModelsScraped.stream().map(carModelMapper::toCarModelDTO).toList()
        ).containsAll(
            carModelsFromDB.stream().map(carModelMapper::toCarModelDTO).toList()
        );
    }
}

