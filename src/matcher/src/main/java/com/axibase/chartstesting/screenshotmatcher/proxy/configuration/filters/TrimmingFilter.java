package com.axibase.chartstesting.screenshotmatcher.proxy.configuration.filters;


import com.axibase.chartstesting.screenshotmatcher.core.Portal;

/**
 * Created by aleksandr on 25.10.16.
 */
public class TrimmingFilter implements LineFilter {
    public String filter(String line, Portal portal) {
        return line.trim();
    }
}
