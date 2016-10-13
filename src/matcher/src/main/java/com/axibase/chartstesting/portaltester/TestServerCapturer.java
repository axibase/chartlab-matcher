package com.axibase.chartstesting.portaltester;

import org.openqa.selenium.WebDriver;

import java.net.URL;

/**
 * Created by aleksandr on 05.10.16.
 */
public class TestServerCapturer extends WebDriverCapturer {
    public TestServerCapturer(WebDriver driver, ChartlabTestingServer server) {
        super(driver, server.getURL());
    }
}
