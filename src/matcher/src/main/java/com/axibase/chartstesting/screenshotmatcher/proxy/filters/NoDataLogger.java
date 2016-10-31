package com.axibase.chartstesting.screenshotmatcher.proxy.filters;

import org.apache.commons.io.output.*;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 * Created by aleksandr on 27.10.16.
 */
public class NoDataLogger implements Filter {
    private final Logger _log = Log.getLogger(NoDataLogger.class);

    private PrintWriter output = null;
    private final Set<String> exclude = new HashSet<String>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String fileName = filterConfig.getInitParameter("output");
        if (fileName == null || fileName.length() == 0) {
            output = new PrintWriter(System.out);
        } else {
            try {
                output = new PrintWriter(fileName);
            } catch (FileNotFoundException e) {
                _log.warn("Unable to init output file. Logging into stdout");
                output = new PrintWriter(System.out);
            }
        }

        String exclFile = filterConfig.getInitParameter("exclude");
        if (exclFile != null && exclFile.length() > 0) {
            JSONParser parser = new JSONParser();
            try (Reader rdr = new FileReader(new File(exclFile))) {
                JSONArray excls = (JSONArray) parser.parse(rdr);
                for (Object o: excls) {
                    exclude.add(o.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                throw new ServletException(e);
            }
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        if (!(resp instanceof HttpServletResponse)) {
            chain.doFilter(req, resp);
            return;
        }

        if (exclude.contains(req.getParameter("proxyId") + "/" + req.getParameter("proxyRev"))) {
            chain.doFilter(req, resp);
            return;
        }

        ResponseTracer httpResp = new ResponseTracer((HttpServletResponse)resp);
        InputStream in = httpResp.getInputStream();
        chain.doFilter(req, httpResp);
        in = new GZIPInputStream(in);
        JSONParser parser = new JSONParser();

        String warn = "[WARN] No data received for portal " +
                req.getParameter("proxyId") + "/" +
                req.getParameter("proxyRev") + "#" +
                req.getParameter("proxyWgt");
        try {
            JSONArray arr = (JSONArray) parser.parse(new InputStreamReader(in));
            if (arr.isEmpty()) {
                output.println(warn);
                output.flush();
                return;
            }

            for (Object obj: arr) {
                JSONObject jsonObj = (JSONObject) obj;
                if (jsonObj.containsKey("data")) {
                    JSONArray data = (JSONArray) jsonObj.get("data");
                    if (data.size() > 0) {
                        continue;
                    }
                    output.println(warn);
                    output.flush();
                } else if (jsonObj.containsKey("textValue")) {
                    continue;
                } else if (jsonObj.containsKey("tags")) {
                    JSONObject tagsObj = (JSONObject) jsonObj.get("tags");
                    if (!tagsObj.isEmpty()) {
                        continue;
                    }
                    output.println(warn);
                    output.flush();
                } else {
                    output.println(warn);
                    output.flush();
                }
            }
        } catch (ParseException e) {
            throw new ServletException(e);
        }

    }

    @Override
    public void destroy() {

    }


    private class ResponseTracer extends HttpServletResponseWrapper {

        private PipedInputStream in = new PipedInputStream();
        private PipedOutputStream out = new PipedOutputStream();
        private TeeOutputStream tee;

        public ResponseTracer(HttpServletResponse response) throws IOException {
            super(response);
            out.connect(in);
            tee = new TeeOutputStream(response.getOutputStream(), out);
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return new PrintWriter(getOutputStream());
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            return new ServletOutputStream() {
                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setWriteListener(WriteListener writeListener) {
                }

                @Override
                public void write(int i) throws IOException {
                    tee.write(i);
                }

                @Override
                public void close() throws IOException {
                    tee.close();
                    out.close();
                }

                @Override
                public void flush() throws IOException {
                    tee.flush();
                    out.flush();
                }
            };
        }

        public InputStream getInputStream() throws IOException {
            return in;
        }
    }
}
