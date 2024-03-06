package io.demo.cars.builders;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HTTPConnectionBuilder {

    public static HttpURLConnection buildHttpConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(100000);
        connection.setReadTimeout(30000);
        connection.connect();

        return connection;
    }
}
