package com.axibase.chartstesting.screenshotmatcher.core;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by aleksandr on 26.09.16.
 */
public class Portal {
    private final String configId;
    private final String revString;
    private String endtime = "none";

    public Portal(String path) throws IllegalArgumentException {
        String[] parts = path.split("/");
        switch (parts.length) {
        case 1:
            configId = parts[0];
            revString = "1";
            break;
        case 2:
            configId = parts[0];
            revString = parts[1];
            break;
        default:
            throw new IllegalArgumentException("unable to parse path " + path);
        }
    }

    @Deprecated
    public Portal(URL url) {
        String[] pathParts = url.getPath().split("/");
        if (pathParts.length < 3) {
            throw new RuntimeException("Bad chartlab URL");
        }
        configId = pathParts[2];
        if (pathParts.length > 3) { // Contains revision
            revString = pathParts[3];
        } else {
            revString = "1";
        }
    }

    public Portal(String id, String rev) {
        configId = id;
        revString = rev != null && rev.length() > 0 ? rev : "1" ;
    }

    public String getConfigId() {
        return configId;
    }

    public String getRevisionString() {
        return revString;
    }

    public String getEndtime() {
        return endtime;
    }

    public void setEndtime(String endtime) {
        this.endtime = endtime == null ? "none": endtime;
    }

    public boolean hasEndtime() {
        return !(endtime.equals("none") || endtime.equals(""));
    }

    @Override
    public String toString() {
        return String.format("%s/%s", configId, revString);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (this.getClass() != other.getClass()) {
            return false;
        }
        Portal otherPortal = (Portal)other;
        return configId.equals(otherPortal.configId) && revString.equals(otherPortal.revString);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
