package com.axibase.chartstesting.screenshotmatcher.matcher.storages;

import com.axibase.chartstesting.screenshotmatcher.core.Portal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by aleksandr on 26.09.16.
 */
public class OutputStorage {
    private static final String OLD_SCREENSHOT_FILE_NAME_FORMAT = "old_%s_%s.png";
    private static final String NEW_SCREENSHOT_FILE_NAME_FORMAT = "new_%s_%s.png";

    private final Path root;
    private final LocalScreenshotStorage oldStorage;
    private final LocalScreenshotStorage newStorage;

    public OutputStorage(String root) throws IOException {
        this(Paths.get(root));
    }

    public OutputStorage(Path root) throws IOException {
        this.root = root;

        this.oldStorage = new LocalScreenshotStorage(root);
        this.newStorage = new LocalScreenshotStorage(root);

        oldStorage.setfileNameFormat(OLD_SCREENSHOT_FILE_NAME_FORMAT);
        newStorage.setfileNameFormat(NEW_SCREENSHOT_FILE_NAME_FORMAT);
    }

    public void save(Portal portal, File oldScreenshot, File newScreenshot) throws IOException {
        oldStorage.save(portal, oldScreenshot);
        newStorage.save(portal, newScreenshot);
    }
}
