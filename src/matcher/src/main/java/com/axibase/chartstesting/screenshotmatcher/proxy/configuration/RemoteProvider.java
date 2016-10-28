package com.axibase.chartstesting.screenshotmatcher.proxy.configuration;


import com.axibase.chartstesting.screenshotmatcher.core.Portal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by aleksandr on 25.10.16.
 */
public class RemoteProvider extends LineFilteringProvider {
    private final URL baseURL;
    public RemoteProvider(URL baseURL) {
        this.baseURL = baseURL;
    }

    @Override
    protected BufferedReader getConfigurationReader(Portal portal) throws IOException {
        String rewritten = baseURL.toString() + portal.getConfigId() + "/" + portal.getRevisionString();
        try {
            return new BufferedReader(new InputStreamReader(new URL(rewritten).openStream()));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
