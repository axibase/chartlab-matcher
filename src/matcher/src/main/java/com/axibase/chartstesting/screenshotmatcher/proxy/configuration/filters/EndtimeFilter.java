package com.axibase.chartstesting.screenshotmatcher.proxy.configuration.filters;

import com.axibase.chartstesting.screenshotmatcher.core.Portal;

/**
 * Created by aleksandr on 01.11.16.
 */
public class EndtimeFilter extends ReplaceMatchingFilterWithExcept implements LineFilter {

    private static final String replacePattern = "[^\\w]*e[^\\w]*n[^\\w]*d[^\\w]*t[^\\w]*i[^\\w]*m[^\\w]*e\\s*=.*";
    private static final String excludePattern = "[^\\w]*e[^\\w]*n[^\\w]*d[^\\w]*t[^\\w]*i[^\\w]*m[^\\w]*e\\s*=\\s*\\d{4}-\\d{2}-\\d{2}.*";
    private static final String replaceWith = "";

    public EndtimeFilter() {

        super(replacePattern, excludePattern, replaceWith);
    }

    @Override
    public String filter(String line, Portal portal) {
        return portal.hasEndtime() ? super.filter(line, portal) : line;
    }
}
