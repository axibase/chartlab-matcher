package com.axibase.chartstesting.screenshotmatcher.proxy.configuration.filters;

import com.axibase.chartstesting.screenshotmatcher.core.Portal;

/**
 * Created by aleksandr on 25.10.16.
 */
public class HeaderFilter implements LineFilter {
    @Override
    public String filter(String line, Portal portal) {
        String header = "endtime = "+portal.getEndtime()+"\n" +
                "timezone = UTC\n" +
                "colors = blueviolet, aquamarine, burlywood, chartreuse, crimson, darkblue, darkgoldenrod, " +
                "darkorange, darkslateblue, darkcyan, deeppink, dodgerblue, greenyellow, green, " +
                "lightgreen, lightsalmon, mediumaquamarine, midnightblue, olivedrab, red, saddlebrown, " +
                "skyblue, slategrey, tomato, yellow, yellowgreen, steelblue, khaki, coral, brown";

        return new ReplaceMatchingFilter("\\[configuration\\]", "[configuration]\n" + header).filter(line, portal);
    }
}
