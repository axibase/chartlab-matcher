package com.axibase.chartstesting.screenshotmatcher.matcher.storages.hash;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by aleksandr on 05.10.16.
 */
public class MD5Hasher implements Hasher {
    public String getHashsum(File file) throws IOException {
        String sumStr = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("md5");
            FileInputStream stream = new FileInputStream(file);
            try {
                int nextByte;
                while ((nextByte = stream.read()) >= 0) {
                    digest.update((byte) nextByte);
                }
                byte[] sum = digest.digest();
                HexBinaryAdapter adapter = new HexBinaryAdapter();
                sumStr = adapter.marshal(sum);
            } finally {
                stream.close();
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sumStr;
    }
}
