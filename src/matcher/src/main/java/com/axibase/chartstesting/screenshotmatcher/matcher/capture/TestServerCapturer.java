package com.axibase.chartstesting.screenshotmatcher.matcher.capture;

import com.axibase.chartstesting.screenshotmatcher.matcher.ChartlabTestingServer;
import org.openqa.selenium.WebDriver;

/**
 * Created by aleksandr on 05.10.16.
 */
public class TestServerCapturer extends WebDriverCapturer {
    public TestServerCapturer(WebDriver driver, ChartlabTestingServer server) {
        super(driver, server.getURL());
    }
}
