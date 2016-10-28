package com.axibase.chartstesting.screenshotmatcher.proxy.configuration.filters;

import com.axibase.chartstesting.screenshotmatcher.core.Portal;

/**
 * Created by aleksandr on 25.10.16.
 */
public class DateReplaceFilter implements LineFilter {
    public String filter(String line, Portal portal) {
        return new ReplaceSubstringFilter("Date ()", "Date("+portal.getEndtime()+")").filter(line, portal);
    }
}
