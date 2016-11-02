package com.axibase.chartstesting.screenshotmatcher.matcher;

/**
 * Created by aleksandr on 02.11.16.
 */
public class WebDriverStopException extends Exception {
    public WebDriverStopException() {
        super();
    }

    public WebDriverStopException(String s) {
        super(s);
    }

    public WebDriverStopException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public WebDriverStopException(Throwable throwable) {
        super(throwable);
    }

    protected WebDriverStopException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
