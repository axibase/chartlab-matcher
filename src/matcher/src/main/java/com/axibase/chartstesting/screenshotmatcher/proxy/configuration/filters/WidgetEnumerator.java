package com.axibase.chartstesting.screenshotmatcher.proxy.configuration.filters;


import com.axibase.chartstesting.screenshotmatcher.core.Portal;

/**
 * Created by aleksandr on 25.10.16.
 */
public class WidgetEnumerator implements LineFilter {
    private int widgetCounter = 1;

    public String filter(String line, Portal portal) {
        if (line.equals("[configuration]")) {
            widgetCounter = 1;
        } else if (line.equals("[widget]")) {
            String urlParameters = String.format("urlparameters = proxyId=%s&proxyRev=%s&proxyWgt=%d",
                    portal.getConfigId(), portal.getRevisionString(), widgetCounter++);
            return "[widget]\n" + urlParameters;
        }
        return line;
    }
}
