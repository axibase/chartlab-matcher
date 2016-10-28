package com.axibase.chartstesting.screenshotmatcher.proxy.configuration.filters;

import com.axibase.chartstesting.screenshotmatcher.core.Portal;

/**
 * Created by aleksandr on 28.10.16.
 */
public class ReplaceSubstringFilter implements LineFilter {
    private final String replaceWith;
    private final String substr;

    public ReplaceSubstringFilter(String substr, String replaceWith) {
        this.substr = substr;
        this.replaceWith = replaceWith;
    }

    @Override
    public String filter(String line, Portal portal) {
        return line.replace(substr, replaceWith);
    }
}
