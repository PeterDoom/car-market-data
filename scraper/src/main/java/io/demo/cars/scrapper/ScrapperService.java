package io.demo.cars.scrapper;

import io.demo.cars.builders.HTTPConnectionBuilder;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class ScrapperService {
    @SneakyThrows
    public Document scrape(String url) {
        HttpURLConnection httpURLConnection = HTTPConnectionBuilder.buildHttpConnection(new URL(url));
        return Jsoup.parse(httpURLConnection.getInputStream(), "windows-1251", "");
    }
}
