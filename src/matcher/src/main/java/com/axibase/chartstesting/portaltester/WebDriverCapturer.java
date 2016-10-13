package com.axibase.chartstesting.portaltester;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Created by aleksandr on 05.10.16.
 */
public class WebDriverCapturer implements ScreenCapturer {
    private static final int DEFAULT_VIEWPORT_WIDTH = 1920;
    private static final int DEFAULT_VIEWPORT_HEIGHT = 1080;

    private final WebDriver driver;
    private final JavascriptExecutor execJS;
    private final TakesScreenshot capture;
    private final URL chartlabURL;

    public WebDriverCapturer(WebDriver driver, URL chartlabURL) {
        this.chartlabURL = chartlabURL;
        if ((driver instanceof TakesScreenshot) && (driver instanceof JavascriptExecutor)) {
            this.driver = driver;
            this.execJS = (JavascriptExecutor) driver;
            this.capture = (TakesScreenshot) driver;
            setViewport(DEFAULT_VIEWPORT_WIDTH, DEFAULT_VIEWPORT_HEIGHT);
        } else {
            throw new RuntimeException("incompatible web driver");
        }
    }

    public File capture(Portal portal) {
        String portalURL = String.format("%s/%s/%s?endtime=%s", chartlabURL,
                portal.getConfigId(), portal.getRevisionString(), portal.getEndtime());
        driver.get(portalURL);
        ExpectedCondition<Boolean> portalReadyCondition = new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver input) {
                JavascriptExecutor exec = (JavascriptExecutor) input;
                return (Boolean) exec.executeScript("return document.isPortalReady();");
            }
        };

        // Wait for data load
        new WebDriverWait(driver, 2 * 60 + 10/*seconds*/)
                .withMessage("Loading tooltip has not disappeared")
                .pollingEvery(500, TimeUnit.MILLISECONDS)
                .until(portalReadyCondition);

        try {
            // Wait for animation
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new TimeoutException();
        }

        return capture.getScreenshotAs(OutputType.FILE);
    }

    public void setViewport(int width, int height) {
        driver.manage().window().setSize(new Dimension(width, height));
    }
}
