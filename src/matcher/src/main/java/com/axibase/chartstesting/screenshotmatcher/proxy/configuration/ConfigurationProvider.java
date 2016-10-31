package com.axibase.chartstesting.screenshotmatcher.proxy.configuration;

import com.axibase.chartstesting.screenshotmatcher.core.Portal;
import com.axibase.chartstesting.screenshotmatcher.proxy.configuration.filters.*;
import org.eclipse.jetty.util.log.StdErrLog;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by aleksandr on 25.10.16.
 */
public abstract class ConfigurationProvider {
    public abstract String getConfiguration(Portal portal) throws IOException;

    public static ConfigurationProvider create() {
        try {
            RemoteProvider provider = new RemoteProvider(new URL("https://apps.axibase.com/chartlab/directories/"));

            provider.appendFilter(new TrimmingFilter());
            //provider.appendFilter(new PropertyNameFilter());
            provider.appendFilter(new WidgetEnumerator());
            provider.appendFilter(new ReplaceMatchingFilterWithExcept("[^\\w]*e[^\\w]*n[^\\w]*d[^\\w]*t[^\\w]*i[^\\w]*m[^\\w]*e\\s*=.*",
                    "[^\\w]*e[^\\w]*n[^\\w]*d[^\\w]*t[^\\w]*i[^\\w]*m[^\\w]*e\\s*=\\s*\\d{4}-\\d{2}-\\d{2}.*", ""));
            provider.appendFilter(new ReplaceSubstringFilter("animat", "noanimat"));
            provider.appendFilter(new DateReplaceFilter());
            provider.appendFilter(new HeaderFilter());

            return provider;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return new NoConfigurationProvider();
    }


    private static class NoConfigurationProvider extends ConfigurationProvider {
        public NoConfigurationProvider() {
            StdErrLog.getLogger(ConfigurationProvider.class).warn("Using no configuration");
        }
        public String getConfiguration(Portal portal) {
            return "";
        }
    }
}
