package com.axibase.chartstesting.screenshotmatcher.matcher.storages;

import com.axibase.chartstesting.screenshotmatcher.core.Portal;

import java.io.File;
import java.io.IOException;

/**
 * Created by aleksandr on 26.09.16.
 */
public interface HashsumStorage {
    String getChecksum(Portal portal) throws IOException;
    void save(Portal portal, File screenshot) throws IOException;
}
