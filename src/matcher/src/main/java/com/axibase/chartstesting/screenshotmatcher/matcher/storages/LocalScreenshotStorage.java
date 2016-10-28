package com.axibase.chartstesting.screenshotmatcher.matcher.storages;

import com.axibase.chartstesting.screenshotmatcher.core.Portal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Created by aleksandr on 05.10.16.
 */
public class LocalScreenshotStorage implements ScreenshotStorage {
    private final Path root;
    private static final String DEFAULT_FILE_NAME_FORMAT = "%s_%s.png"; // <id>_<rev>.png (e.g. deadbeef_1.png)
    private String fileNameFormat = DEFAULT_FILE_NAME_FORMAT;

    public LocalScreenshotStorage(String root) throws IOException {
        this(Paths.get(root));
    }

    public LocalScreenshotStorage(Path root) throws IOException {
        this.root = root;
        Files.createDirectories(root);
    }

    public File getScreenshot(Portal portal) {
        return getFilePath(portal).toFile();
    }

    public void save(Portal portal, File screenshot) throws IOException {
        Path filePath = getFilePath(portal);
        if (Files.notExists(filePath)) {
            Files.createFile(filePath);
        }
        InputStream in = new FileInputStream(screenshot);
        Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
    }

    public boolean contains(Portal portal) {
        return Files.exists(getFilePath(portal));
    }

    private Path getFilePath(Portal portal) {
        String fileName = String.format(fileNameFormat, portal.getConfigId(), portal.getRevisionString());
        return root.resolve(fileName);
    }

    public void setfileNameFormat(String format) {
        fileNameFormat = format;
    }
}
