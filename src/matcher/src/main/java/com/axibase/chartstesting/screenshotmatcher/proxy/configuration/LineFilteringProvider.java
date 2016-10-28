package com.axibase.chartstesting.screenshotmatcher.proxy.configuration;


import com.axibase.chartstesting.screenshotmatcher.core.Portal;
import com.axibase.chartstesting.screenshotmatcher.proxy.configuration.filters.LineFilter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Created by aleksandr on 25.10.16.
 */
public abstract class LineFilteringProvider extends ConfigurationProvider {
    private final LinkedList<LineFilter> filters = new LinkedList<LineFilter>();

    private boolean joiningLines = false;

    @Override
    public String getConfiguration(Portal portal) throws IOException {
        BufferedReader confSrc = getConfigurationReader(portal);
        StringBuilder confBuilder = new StringBuilder();
        String line;
        while ((line = confSrc.readLine()) != null) {
            String filtered = line;
            for (LineFilter filter : filters) {
                filtered = filter.filter(filtered, portal);
            }
            confBuilder.append(filtered);
            confBuilder.append('\n');
        }
        return confBuilder.toString();
    }

    public void appendFilter(LineFilter filter) {
        filters.add(filter);
    }

    public void setJoiningLines(boolean joiningLines) {
        this.joiningLines = joiningLines;
    }

    public boolean isJoiningLines() {
        return joiningLines;
    }

    protected abstract BufferedReader getConfigurationReader(Portal portal) throws IOException;
}
