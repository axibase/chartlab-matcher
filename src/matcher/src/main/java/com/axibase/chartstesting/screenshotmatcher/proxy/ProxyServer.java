package com.axibase.chartstesting.screenshotmatcher.proxy;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.log.StdErrLog;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Created by aleksandr on 25.10.16.
 */
public class ProxyServer {
    public static final int DEFAULT_PORT = 8000;
    private static int port = DEFAULT_PORT;

    private static Logger _log = Log.getLogger(ProxyServer.class);

    public static void setPort(int port) {
        ProxyServer.port = port;
    }

    public static void main(String[] args) {
        QueuedThreadPool pool = new QueuedThreadPool(100, 10);
        Server server = new Server(pool);

        ServerConnector http = new ServerConnector(server);
        http.setHost("localhost");
        http.setPort(port);
        http.setIdleTimeout(120000);
        server.addConnector(http);

        WebAppContext context = new WebAppContext();

        context.setContextPath("/");
        context.setResourceBase("src/main/webapp");
        context.setDescriptor("WEB-INF/web.xml");
        server.setHandler(context);

        try {
            _log.info("Starting server on :" + port);
            server.start();
            server.join();
        } catch (InterruptedException e) {
            _log.info("Server stopped");
        } catch (Exception e) {
            _log.warn("Unable to start server on :" + port, e);
        }
    }
}
