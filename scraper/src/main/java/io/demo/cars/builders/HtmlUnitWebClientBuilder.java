package io.demo.cars.builders;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HtmlUnitWebClientBuilder {

    public static WebClient buildWebClient() {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getCurrentWindow().getJobManager().removeAllJobs();
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);

        return webClient;
    }
}
