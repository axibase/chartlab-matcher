package com.axibase.chartstesting.screenshotmatcher.matcher;

import com.axibase.chartstesting.screenshotmatcher.proxy.ProxyServer;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by aleksandr on 05.10.16.
 */
public class ChartlabTestingServer {
    private static final Logger _log = Log.getLogger(ChartlabTestingServer.class);

    private final int port;
    private Thread serverThrd;

    private String logFile = null;

    public ChartlabTestingServer() throws IOException {
        this(ProxyServer.DEFAULT_PORT);
    }

    public ChartlabTestingServer(int port) throws IOException {
        this.port = port;
    }

    public void start() throws IOException {
        serverThrd = new Thread(new Runnable() {
            @Override
            public void run() {
                ProxyServer.setPort(port);
                ProxyServer.main(null);
                _log.info("Starting test server on " + getURL());
            }
        });
        serverThrd.setDaemon(true);
        serverThrd.start();
    }

    @Deprecated
    public void writeOutputTo(String logFile) {
        this.logFile = logFile;
    }

    public URL getURL() {
        try {
            return new URL("http", "localhost", port, "");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        if (serverThrd != null) {
            serverThrd.interrupt();
            _log.info("Test server shut down");
        }
    }

    public void kill() {
        close();
    }

}
