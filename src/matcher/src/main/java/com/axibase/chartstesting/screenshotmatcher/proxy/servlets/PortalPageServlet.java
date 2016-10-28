package com.axibase.chartstesting.screenshotmatcher.proxy.servlets;

import com.axibase.chartstesting.screenshotmatcher.core.Portal;
import com.axibase.chartstesting.screenshotmatcher.proxy.configuration.ConfigurationProvider;
import org.apache.commons.lang.StringEscapeUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by aleksandr on 25.10.16.
 */
public class PortalPageServlet extends HttpServlet {
    private ConfigurationProvider confProvider = ConfigurationProvider.create();

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("text/html");

        resp.getWriter().print(beforeConfiguration);

        Portal portal = readPortalFromRequest(req);
        if (portal == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String config = confProvider.getConfiguration(portal);
        resp.getWriter().print(StringEscapeUtils.escapeJavaScript(config));
        resp.getWriter().print(afterConfiguration);

        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private Portal readPortalFromRequest(HttpServletRequest req) {
        String[] parts = req.getPathInfo().split("/");
        Portal portal;
        switch (parts.length) {
            case 2:
                portal = new Portal(parts[1], null);
                break;
            case 3:
                portal = new Portal(parts[1], parts[2]);
                break;
            default:
                return null;
        }
        portal.setEndtime(req.getParameter("endtime"));
        return portal;
    }

    private static String beforeConfiguration = "<!DOCTYPE html>\n" +
            "<meta charset=\"utf-8\">\n" +
            "<title>Portal</title>\n" +
            "<link rel=\"stylesheet\" href=\"/assets/JavaScript/jquery-ui-1.9.0.custom/css/smoothness/jquery-ui-1.9.1.custom.min.css\">\n" +
            "<link rel=\"stylesheet\" href=\"/assets/CSS/charts.min.css\">\n" +
            "<script src=\"/assets/JavaScript/portal_init.js\"></script>\n" +
            "<script src=\"/assets/JavaScript/jquery-ui-1.9.0.custom/js/jquery-1.8.2.min.js\"></script>\n" +
            "<script src=\"/assets/JavaScript/jquery-ui-1.9.0.custom/js/jquery-ui-1.9.0.custom.min.js\"></script>\n" +
            "<script src=\"/assets/JavaScript/d3.min.js\"></script>\n" +
            "<script src=\"/assets/JavaScript/highlight.pack.js\"></script>\n" +
            "<script src=\"/assets/JavaScript/charts.min.js\"></script>\n" +
            "<style>\n" +
            "  * {\n" +
            "    font-family: monospace !important;\n" +
            "    font-smooth: never !important;\n" +
            "    -webkit-font-smoothing: none !important;\n" +
            "  }\n" +
            "</style>\n" +
            "<body onload=\"onBodyLoad()\">\n" +
            "  <script>\n" +
            "    document.isPortalReady = function() {\n" +
            "      return (function () {\n" +
            "        // Any tooltip loaded\n" +
            "        return $('.axi-tooltip').size() > 0;\n" +
            "      })() && (function () {\n" +
            "        var loadingTooltips = $('.axi-tooltip-inner:contains(\"Load\")');\n" +
            "\n" +
            "        // Any loading tooltip exists in DOM\n" +
            "        var ltex = (loadingTooltips.size() > 0)\n" +
            "\n" +
            "        // All loading tooltips are invisible\n" +
            "        return ltex && !loadingTooltips.is(\":visible\");\n" +
            "      })()\n" +
            "    };\n" +
            "\n" +
            "    initializePortal(function() {\n" +
            "      var configText = \"";

    private static String afterConfiguration = "\";\n" +
                    "      return [configText, window.portalPlaceholders = getPortalPlaceholders()];\n" +
                    "    });\n" +
                    "  </script>\n" +
                    "  <div class=\"portalView\"></div>\n" +
                    "  <div id=\"dialog\"></div>\n" +
                    "</body>";
}
