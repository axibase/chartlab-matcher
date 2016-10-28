package com.axibase.chartstesting.screenshotmatcher.proxy.filters;


import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Created by aleksandr on 27.10.16.
 */
public class CacheFilter implements Filter {

    private static final String CACHE_FILE_NAME_FORMAT = "%s_%s_%s.cache";
    private final Logger _log = Log.getLogger(CacheFilter.class);
    private String cacheDir = null;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        cacheDir = filterConfig.getInitParameter("cache");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        if (!(req instanceof HttpServletRequest)) {
            chain.doFilter(req, resp);
            return;
        }

        HttpServletRequest httpReq = new MultiReadRequest((HttpServletRequest)req);
        Path cachePath = getCacheFilePath(httpReq);
        if (cachePath != null && Files.exists(cachePath)) {
            serveCache(cachePath, resp);
            return;
        }
        chain.doFilter(httpReq, resp);
    }

    @Override
    public void destroy() {

    }


    private void serveCache(Path cachePath, ServletResponse resp) throws IOException {
        Files.copy(cachePath, resp.getOutputStream());
        _log.info("Cache used for " + cachePath.getFileName());
    }

    private Path getCacheFilePath(HttpServletRequest req) {
        String path = req.getServletPath() + req.getPathInfo();
        if (!usesCache()) return null;
        if (path.length() < 2) return null;

        path = path.substring(1);
        String[] pathParts = path.split("/");
        Path cacheFileDir = Paths.get(cacheDir, pathParts);
        String cacheFileName = String.format(CACHE_FILE_NAME_FORMAT,
                req.getParameter("proxyId"),
                req.getParameter("proxyRev"),
                req.getParameter("proxyWgt"));

        if (forecastRequested(req)) {
            cacheFileName = "forecast_" + cacheFileName;
        }

        return cacheFileDir.resolve(cacheFileName);
    }

    private boolean forecastRequested(HttpServletRequest req) {
        try {
            return new Scanner(new HttpServletRequestWrapper(req).getInputStream())
                    .findInLine(Pattern.compile("forecast", Pattern.CASE_INSENSITIVE)) != null;
        } catch (IOException e) {
            _log.warn("Exception during reading request ", e);
            return false;
        }
    }

    public String getCacheDir() {
        return cacheDir;
    }

    public void setCacheDir(String cacheDir) {
        this.cacheDir = cacheDir;
        try {
            Files.createDirectories(Paths.get(cacheDir));
        } catch (IOException e) {
            _log.warn("Unable to initialize cache on "+cacheDir, e);
            this.cacheDir = null;
        }
    }

    public boolean usesCache() {
        return cacheDir != null;
    }

    private class MultiReadRequest extends HttpServletRequestWrapper {
        private byte[] bytes;



        public MultiReadRequest(HttpServletRequest request) throws IOException {
            super(request);

            StringBuilder contentBuilder = new StringBuilder();
            BufferedReader reader = new BufferedReader(request.getReader());
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line);
            }
            bytes = contentBuilder.toString().getBytes(Charset.forName("utf-8"));
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return new BufferedReader(new InputStreamReader(getInputStream()));
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            return new ServletInputStream() {
                private final ByteArrayInputStream bytesStream = new ByteArrayInputStream(bytes);

                @Override
                public boolean isFinished() {
                    return false;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {

                }

                @Override
                public int read() throws IOException {
                    return bytesStream.read();
                }
            };
        }
    }

}
