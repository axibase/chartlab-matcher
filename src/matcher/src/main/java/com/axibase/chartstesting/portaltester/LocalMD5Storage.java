package com.axibase.chartstesting.portaltester;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by aleksandr on 05.10.16.
 */
public class LocalMD5Storage extends LocalHashsumStorage {
    public LocalMD5Storage(String root) throws IOException {
        this(Paths.get(root));
    }

    public LocalMD5Storage(Path root) throws IOException {
        super(root, new MD5Hasher(), "%s_%s.md5");
    }
}
