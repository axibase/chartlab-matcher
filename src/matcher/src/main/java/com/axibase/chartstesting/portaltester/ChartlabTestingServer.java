package com.axibase.chartstesting.portaltester;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by aleksandr on 05.10.16.
 */
public class ChartlabTestingServer {
    private static final Logger log = Logger.getLogger(ChartlabTestingServer.class.getName());

    private final String addr;
    private final URL url;
    private final String exec;
    private Process serverProc = null;

    private String logFile = null;

    public ChartlabTestingServer() throws IOException {
        this("configurator");
    }

    public ChartlabTestingServer(String exec) throws IOException {
        this(exec, ":8080");
    }

    public ChartlabTestingServer(String exec, String addr) throws IOException {
        this.addr = addr;
        this.exec = exec;
        addr = addr.trim();
        if (addr.startsWith(":")) {
            addr = "http://localhost" + addr;
        } else if (!addr.contains(":/")) {
            addr = "http://" + addr;
        }
        url = new URL(addr);
    }

    public void start() throws IOException {
        log.info("Starting test server on " + url.toString());
        ProcessBuilder builder;
        if (logFile == null) {
            builder = new ProcessBuilder(exec, "--addr", addr);
        } else {
            builder = new ProcessBuilder(exec, "--addr", addr, "--log", logFile);
        }
        serverProc = builder.start();
    }

    public void writeOutputTo(String logFile) {
        this.logFile = logFile;
    }

    public URL getURL() {
        return url;
    }

    public void close() {
        if (serverProc != null) {
            serverProc.destroy();
            log.info("Test server shut down");
        }
    }

    public void kill() {
        close();
    }

}
