package com.axibase.chartstesting.screenshotmatcher.proxy.configuration.filters;


import com.axibase.chartstesting.screenshotmatcher.core.Portal;
import org.eclipse.jetty.util.log.Log;

/**
 * Created by aleksandr on 25.10.16.
 */
public class PropertyNameFilter implements LineFilter {
    public String filter(String line, Portal portal) {
        if (!line.contains("=")) {
            return line;
        }
        char[] arr = line.toCharArray();
        StringBuilder output = new StringBuilder();
        boolean inPropName = true;
        for (char c: arr) {
            switch (c) {
            case '=':
            case '[':
                inPropName = false;
                break;
            case ' ':
            case '\t':
                break;
            default:
                if (inPropName && !Character.isLetterOrDigit(c)) continue;
            }
            output.append(c);
        }
        return output.toString();
    }
}
