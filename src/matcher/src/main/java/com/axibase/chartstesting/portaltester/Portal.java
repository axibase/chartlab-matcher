package com.axibase.chartstesting.portaltester;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by aleksandr on 26.09.16.
 */
public class Portal {
    private final URL url;
    private final String configId;
    private final String revString;
    private String endtime = null;

    public Portal(String url) throws MalformedURLException {
        this(new URL(url));
    }

    public Portal(URL url) {
        if (url.getProtocol().equals("http")) {
            try {
                url = new URL("https", url.getHost(), url.getPort(), url.getFile());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        this.url = url;
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

    public URL getURL() {
        return url;
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

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (this.getClass() != other.getClass()) {
            return false;
        }
        return url.equals(((Portal)other).url);
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }
}
