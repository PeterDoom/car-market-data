package io.demo.cars.utils;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PageProcessor {

    public static int retrieveMaxPages(WebClient webClient, String url, String querySelector) throws IOException {
        HtmlPage rawPage = webClient.getPage(url);
        String pageNumberText = rawPage.querySelector(querySelector).asNormalizedText();
        return Integer.parseInt(pageNumberText.split(" ")[3]);
    }

    public static List<String> getAllModelsByBrand(WebClient webClient, String url, String brand) {
        try {
            HtmlPage page = webClient.getPage(url);
            HtmlSelect brandSelect = page.getElementByName("marka");
            HtmlOption option = brandSelect.getOptionByValue(brand);
            brandSelect.setSelectedAttribute(option, true);
            HtmlSelect modelSelect = page.getElementByName("model");

            return modelSelect.getOptions()
                .stream()
                .map(HtmlOption::getText)
                .toList();
        } catch (IOException e) {
            log.warn("An error occurred while getting models");
        }

        return Collections.emptyList();
    }
}
