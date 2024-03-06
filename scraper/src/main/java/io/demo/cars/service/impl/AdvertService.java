package io.demo.cars.service.impl;

import com.gargoylesoftware.htmlunit.WebClient;
import io.demo.cars.builders.HtmlUnitWebClientBuilder;
import io.demo.cars.config.CarFilterUrlMap;
import io.demo.cars.model.CarModel;
import io.demo.cars.model.RawCarAdvert;
import io.demo.cars.repository.RawCarAdvertRepository;
import io.demo.cars.scrapper.ScrapperService;
import io.demo.cars.service.ScraperService;
import io.demo.cars.utils.PageProcessor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdvertService implements ScraperService {

    private final ScrapperService scrapperService;
    private final RawCarAdvertRepository rawCarAdvertRepository;
    private final CarFilterUrlMap carFilterUrlMap;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @SneakyThrows
    @Override
    public void scrape() {
        Map<CarModel, String> carFilterUrlMapValue = carFilterUrlMap.getCarFilterUrlMap();
        LocalDateTime startTime = LocalDateTime.now();

        carFilterUrlMapValue.forEach((key, value) -> {
            log.info("Scraping for:{}/{}", key.getId(), key.getBrandName());
            scrapeByLink(key, value);
        });
        shutdownAndAwaitTermination(executorService);
        log.info("Total time for scraping adverts: {} ms.", ChronoUnit.MILLIS.between(startTime, LocalDateTime.now()));
    }

    private void scrapeByLink(CarModel carModel, String pageLink) {
        WebClient webClient = HtmlUnitWebClientBuilder.buildWebClient();
        webClient.getOptions().setJavaScriptEnabled(false);

        int pagesNumber = getPagesNumber(webClient, pageLink);
        IntStream.range(1, pagesNumber + 1)
            .forEach(page -> {
                Document advertPage = scrapperService.scrape(generateAdvertUrlByPageNumber(pageLink, page));
                executorService.submit(() -> fetchAdvertsForPage(carModel, advertPage));
            });
    }

    private void fetchAdvertsForPage(CarModel carModel, Document advertPage) {
        Optional<Element> searchForm = advertPage.select("form[name=search]")
            .stream()
            .findFirst();
        if (searchForm.isEmpty()) {
            log.error("Cannot scrape advert without present form with class='search'");
            throw new RuntimeException();
        }
        Elements tableReset = searchForm.get().getElementsByClass("tablereset");

        for (int i = 1; i < tableReset.size(); i++) {
            Element advert = tableReset.get(i);
            String advertLink = advert.getElementsByClass("mmm").attr("href");
            if (!advertLink.isEmpty()) {
                fetchIndividualAdvert(carModel, advertLink);
            }
        }
    }

    private void fetchIndividualAdvert(CarModel carModel, String advertLink) {
        advertLink = "https:".concat(advertLink);
        Document scrappedAdvert = scrapperService.scrape(advertLink);
        String advertPrice = Objects.requireNonNull(scrappedAdvert.getElementById("details_price")).text();
        String advertAddress = scrappedAdvert.getElementsByClass("adress").text();
        Elements visitCountDiv = scrappedAdvert.select("span[class=advact]");

        RawCarAdvert uncleanCarAdvert = RawCarAdvert.builder()
            .advertLink(advertLink)
            .carModel(carModel)
            .extras(buildExtras(scrappedAdvert))
            .descriptionFromStart(getAdvertDescription(scrappedAdvert))
            .scrapedAt(Instant.now().toEpochMilli())
            .advertNumber(HttpUrl.parse(advertLink).queryParameter("adv"))
            .visitCount(visitCountDiv.text())
            .lastUpdatedBySeller(getLastUpdatedAtBySeller(visitCountDiv))
            .price(advertPrice)
            .currency(advertPrice.contains("лв.") ? "BGN" : "EUR")
            .location(advertAddress)
            .sellerType(advertAddress.contains("Дилър") ? "Dealer" : "Private seller")
            .year(getPropertyFromDilarData("Дата на производство", scrappedAdvert))
            .horsepower(getPropertyFromDilarData("Мощност", scrappedAdvert))
            .engineInfo(getPropertyFromDilarData("Тип двигател", scrappedAdvert))
            .gearboxType(getPropertyFromDilarData("Скоростна кутия", scrappedAdvert))
            .cubicCapacity(getPropertyFromDilarData("Кубатура [куб.см]", scrappedAdvert))
            .color(getPropertyFromDilarData("Цвят", scrappedAdvert))
            .mileage(getPropertyFromDilarData("Пробег [км]", scrappedAdvert))
            .coupeType(getPropertyFromDilarData("Категория", scrappedAdvert))
            .build();

        rawCarAdvertRepository.save(uncleanCarAdvert);
    }

    private String getLastUpdatedAtBySeller(Elements visitCountDiv) {
        //We need the visit count div in order to get the last updated at, because they are sibling divs inside one common div
        Optional<Element> firstElement = visitCountDiv.stream().findFirst();
        if (firstElement.isEmpty()) {
            return null;
        }
        Elements siblingElements = firstElement.get().siblingElements();
        Optional<Element> lastUpdatedBySellerDiv = siblingElements.stream()
            .filter(element -> element.text().contains("Редактирана") || element.text().contains("Публикувана"))
            .findFirst();
        return lastUpdatedBySellerDiv.map(Element::text).orElse(null);
    }

    private String getPropertyFromDilarData(String propertyName, Document scrappedAdvert) {
        Elements dilarData = scrappedAdvert.getElementsByClass("dilarData");
        Optional<Element> advertInformation = dilarData.stream().findFirst();

        if (advertInformation.isEmpty()) {
            log.error("Cannot scrape advert information. No dilarData found!");
            throw new RuntimeException();
        }

        Elements dilarDataElements = advertInformation.get().children();
        for (int i = 0; i < dilarDataElements.size(); i++) {
            if (dilarDataElements.get(i).text().equalsIgnoreCase(propertyName)) {
                return dilarDataElements.get(i + 1).text();
            }
        }
        return null;
    }

    private String getAdvertDescription(Document scrappedAdvert) {
        Optional<Element> scrappedSearchForm = scrappedAdvert.select("form[name=search]").stream().findFirst();
        if (scrappedSearchForm.isEmpty()) {
            return null;
        }
        Element searchForm = scrappedSearchForm.get();
        Elements searchFormChildren = searchForm.children();
        Element descriptionContainer = searchFormChildren.get(24);
        Elements descriptionContainerChildren = descriptionContainer.children();
        Element descriptionText = descriptionContainerChildren.first();
        if (descriptionText == null) {
            return null;
        }
        return descriptionText.text();
    }

    @SneakyThrows
    private static int getPagesNumber(WebClient webClient, String advertUrl) {
        int pageNumber = PageProcessor.retrieveMaxPages(
            webClient,
            advertUrl,
            ".pageNumbersInfo b"
        );
        log.info("Current advert pages number: {}", pageNumber);
        return pageNumber;
    }

    private String buildExtras(Document scrappedAdvert) {
        Elements extrasContainer = scrappedAdvert.select("td[valign=top]");
        StringBuilder result = new StringBuilder();
        IntStream.range(5, 8)
            .filter(index -> extrasContainer.get(index) != null)
            .forEach(index -> result.append(extrasContainer.get(index).text()).append(System.lineSeparator()));
        return result.toString();
    }

    private static String generateAdvertUrlByPageNumber(String advertUrl, int pageNumber) {
        return advertUrl.substring(0, advertUrl.length() - 1) + pageNumber;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown();
        try {
            pool.awaitTermination(20, TimeUnit.SECONDS);
            pool.shutdownNow();
        } catch (InterruptedException ie) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
