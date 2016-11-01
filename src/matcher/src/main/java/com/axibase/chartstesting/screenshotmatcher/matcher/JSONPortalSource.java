package com.axibase.chartstesting.screenshotmatcher.matcher;

import com.axibase.chartstesting.screenshotmatcher.core.Portal;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by aleksandr on 26.09.16.
 */
public class JSONPortalSource implements PortalSource {

    private HashSet<Portal> portalSet = new HashSet<Portal>();
    private Iterator<Portal> portalIterator = portalSet.iterator();
    public synchronized Portal get() {
        if (portalIterator.hasNext()) {
            return portalIterator.next();
        }
        return null;
    }

    public void includeFile(String filePath) throws ParseException, IOException {
        if (filePath == null | filePath.length() == 0) {
            return;
        }
        includeFile(new File(filePath));
    }

    public void includeFile(File file) throws ParseException, IOException {
        Reader input = new FileReader(file);
        try {
            JSONParser parser = new JSONParser();
            JSONArray jsonArr = (JSONArray) parser.parse(input);
            for (Object obj : jsonArr) {
                JSONObject json = (JSONObject) obj;
                Portal page = new Portal(json.get("path").toString());

                Object endtime = json.get("endtime");
                String et = null;
                if (endtime != null) {
                    et = endtime.toString();
                }
                page.setEndtime(et);

                portalSet.add(page);
            }
        } finally {
            input.close();
            portalIterator = portalSet.iterator();
        }
    }

    public void ignoreFile(String filePath) throws ParseException, IOException {
        if (filePath == null || filePath.length() == 0) {
            return;
        }
        ignoreFile(new File(filePath));
    }

    public void ignoreFile(File file) throws ParseException, IOException {
        try {
            HashSet<Portal> bufferSet = portalSet;
            portalSet = new HashSet<Portal>();
            includeFile(file);
            bufferSet.removeAll(portalSet);
            portalSet = bufferSet;
        } finally {
            portalIterator = portalSet.iterator();
        }
    }
}
