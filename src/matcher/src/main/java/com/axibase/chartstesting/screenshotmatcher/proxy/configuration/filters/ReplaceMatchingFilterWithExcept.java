package com.axibase.chartstesting.screenshotmatcher.proxy.configuration.filters;


import com.axibase.chartstesting.screenshotmatcher.core.Portal;

import java.util.regex.Pattern;

/**
 * Created by aleksandr on 25.10.16.
 */
public class ReplaceMatchingFilterWithExcept extends ReplaceMatchingFilter {
    protected final Pattern except;

    public ReplaceMatchingFilterWithExcept(String regex, String exceptRegex, String replaceWith) {
        super(regex, replaceWith);
        except = Pattern.compile(exceptRegex);
    }

    @Override
    public String filter(String line, Portal portal) {
        return except.matcher(line).matches() ? line : super.filter(line, portal);
    }
}
