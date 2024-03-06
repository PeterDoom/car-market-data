package io.demo.cars.service.impl;

import io.demo.cars.config.CarFilterUrlMap;
import io.demo.cars.model.CarBrand;
import io.demo.cars.model.CarModel;
import io.demo.cars.repository.CarBrandRepository;
import io.demo.cars.service.ScraperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.demo.cars.constant.Constants.MOBILE_BG_BASE_URL;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilterFormService implements ScraperService {

    private final CarFilterUrlMap carFilterUrlMapBean;

    private final CarBrandRepository carBrandRepository;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Override
    public void scrape() {
        List<CarBrand> carBrandList = carBrandRepository.findAll();
        carBrandList.forEach(
            carBrand -> executorService.submit(() -> getSlinkForMany(carBrand.getModels()))
        );
        shutdownAndAwaitTermination(executorService);
    }

    private void getSlinkForBrandModelPair(CarModel car) {

        String carModel = car.getBrandName();
        String carBrand = car.getBrand().getBrandName();

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("act", "3")
            .addFormDataPart("rub", "1")
            .addFormDataPart("marka", carBrand)
            .addFormDataPart("model", carModel)
            .build();

        log.info("Sending slink request for {} {}", carBrand, carModel);

        Request request = new Request.Builder()
            .url(MOBILE_BG_BASE_URL)
            .method("POST", body)
            .build();
        try {
            Response response = client.newCall(request).execute();
            log.info("Received response from server");
            carFilterUrlMapBean.getCarFilterUrlMap().put(car, response.request().url().url().toString());
            log.info("Slink for brand-model pair: {}/{} is {}", carBrand, carModel, response.request().url().url());
            response.close();
        } catch (Exception e) {
            log.error("Error");
        }
    }

    private void getSlinkForMany(List<CarModel> carList) {
        carList.forEach(this::getSlinkForBrandModelPair);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown();
        try {
            pool.awaitTermination(100, TimeUnit.SECONDS);
            pool.shutdownNow();
        } catch (InterruptedException ie) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
