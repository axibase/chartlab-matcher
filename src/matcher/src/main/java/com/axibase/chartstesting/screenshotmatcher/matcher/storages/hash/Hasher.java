package com.axibase.chartstesting.screenshotmatcher.matcher.storages.hash;

import java.io.File;
import java.io.IOException;

/**
 * Created by aleksandr on 26.09.16.
 */
public interface Hasher {
    String getHashsum(File file) throws IOException;
}
