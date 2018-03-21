package com.axibase.chartstesting.screenshotmatcher.matcher.storages;

import com.axibase.chartstesting.screenshotmatcher.core.Portal;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by aleksandr on 26.09.16.
 */
public interface HashsumStorage {
    @Deprecated
    String getChecksum(Portal portal) throws IOException;
    List<String> getChecksums(Portal portal) throws IOException;
    void save(Portal portal, File screenshot) throws IOException;
}
