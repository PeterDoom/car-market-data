package io.demo.cars.execution;

import io.demo.cars.builders.HTTPConnectionBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;

@Slf4j
@RequiredArgsConstructor
public class ScraperTask implements Callable<InputStream> {

    private final String urlAsString;

    public InputStream call() {
        try {
            URL url = new URL(urlAsString);
            HttpURLConnection conn = HTTPConnectionBuilder.buildHttpConnection(url);
            log.info("Beginning to scrape: {}", url);

            InputStream in;
            if (conn.getResponseCode() >= 400) {
                in = conn.getErrorStream();
                return in;
            } else {
                log.info("Scraped: {}", url);
                return conn.getInputStream();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
