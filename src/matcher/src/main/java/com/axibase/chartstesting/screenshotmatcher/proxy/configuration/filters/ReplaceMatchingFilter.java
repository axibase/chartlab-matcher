package com.axibase.chartstesting.screenshotmatcher.proxy.configuration.filters;


import com.axibase.chartstesting.screenshotmatcher.core.Portal;

import java.util.regex.Pattern;

/**
 * Created by aleksandr on 25.10.16.
 */
public class ReplaceMatchingFilter implements LineFilter {
    private final Pattern pattern;
    private final String replaceWith;

    public ReplaceMatchingFilter(String regex, String replaceWith) {
        this.pattern = Pattern.compile(regex);
        this.replaceWith = replaceWith;
    }

    public String filter(String line, Portal portal) {
        return pattern.matcher(line).matches() ? replaceWith : line;
    }
}
