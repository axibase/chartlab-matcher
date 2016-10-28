package com.axibase.chartstesting.screenshotmatcher.proxy.servlets;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 * Created by aleksandr on 27.10.16.
 */
public class AssetsServlet extends ProxyServlet.Transparent {
    @Override
    protected HttpClient newHttpClient() {
        SslContextFactory factory = new SslContextFactory();
        factory.setTrustAll(true);
        return new HttpClient(factory);
    }

}
