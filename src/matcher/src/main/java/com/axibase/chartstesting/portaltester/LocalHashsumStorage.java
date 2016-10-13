package com.axibase.chartstesting.portaltester;

import org.apache.commons.io.IOExceptionWithCause;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by aleksandr on 05.10.16.
 */
public abstract class LocalHashsumStorage implements HashsumStorage {
    protected final Hasher hasher;
    protected final String fileNameFormat;

    private final Path root;

    protected LocalHashsumStorage(String root, Hasher hasher, String format) throws IOException {
        this(Paths.get(root), hasher, format);
    }

    protected LocalHashsumStorage(Path root, Hasher hasher, String format) throws IOException {
        this.root = root;
        this.hasher = hasher;
        this.fileNameFormat = format;

        Files.createDirectories(root);
    }

    public String getChecksum(Portal portal) throws IOException {
        File file = getFilePath(portal).toFile();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();
        if (line == null) {
            throw new IOException("Bad hashsum file");
        }
        return line;
    }

    public void save(Portal portal, File screenshot) throws IOException {
        String sum = hasher.getHashsum(screenshot);
        Path filePath = getFilePath(portal);
        if (Files.notExists(filePath)) {
            Files.createFile(filePath);
        }
        FileWriter writer =new FileWriter(filePath.toFile());
        try {
            writer.write(sum);
        } finally {
            writer.close();
        }
    }

    public Hasher getHasher() {
        return hasher;
    }

    private Path getFilePath(Portal portal) {
        String fileName = String.format(fileNameFormat, portal.getConfigId(), portal.getRevisionString());
        return root.resolve(fileName);
    }
}
