package com.axibase.chartstesting.screenshotmatcher.matcher.capture;

import com.axibase.chartstesting.screenshotmatcher.core.Portal;

import java.io.File;

/**
 * Created by aleksandr on 26.09.16.
 */
public interface ScreenCapturer {
    File capture(Portal portal);
}
