package com.axibase.chartstesting.screenshotmatcher.proxy.configuration.filters;

import com.axibase.chartstesting.screenshotmatcher.core.Portal;

/**
 * Created by aleksandr on 25.10.16.
 */
public interface LineFilter {
    String filter(String line, Portal portal);
}
