package com.arc.util.file;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.security.MessageDigest;


public class FileSameCheckTool {

    private static final Logger log = LoggerFactory.getLogger(FileSameCheckTool.class);

    // 计算文件的SHA-256哈希值
    public static String[] calculateHash(File file) {
        try {
            MessageDigest digestAES256 = MessageDigest.getInstance("SHA-256");
            MessageDigest digestMD5 = MessageDigest.getInstance("MD5");
            try (InputStream is = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    digestAES256.update(buffer, 0, bytesRead);
                    digestMD5.update(buffer, 0, bytesRead);
                }
            }
            return new String[]{bytesToHex(digestAES256.digest()), bytesToHex(digestMD5.digest())};

        } catch (Exception exception) {
            log.error("Error calculateHash", exception);
            return new String[]{};
        }
    }

    public static String calculateHashSHA256(File file) {
        try {
            MessageDigest digestAES256 = MessageDigest.getInstance("SHA-256");
            try (InputStream is = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    digestAES256.update(buffer, 0, bytesRead);
                }
            }
            return bytesToHex(digestAES256.digest());

        } catch (Exception exception) {
            log.error("Error calculateHash", exception);
            return null;
        }

    }

    public static String calculateHashSHA256( byte[] content) {
        try {
            return calculate(MessageDigest.getInstance("SHA-256"), content);
        } catch (Exception exception) {
            log.error("Error calculateHash", exception);
            return null;
        }
    }
    public static String calculate(MessageDigest digest, byte[] content) {
        return calculateCloseable(digest,new ByteArrayInputStream(content));
    }

    public static String calculate(MessageDigest digest, String content, Charset charset) {
        // StandardCharsets.UTF_8
        return calculateCloseable(digest,new ByteArrayInputStream(content.getBytes(charset)));
    }

    public static String calculate(MessageDigest digest, File file) {
        try {
            return calculateCloseable(digest,new FileInputStream(file)  );
        } catch (FileNotFoundException exception) {
            log.error("Error calculate", exception);
            throw new RuntimeException(exception);
        }

    }

    public static String calculateHashSHA256(   InputStream is) {
        try {
            return calculateCloseable(MessageDigest.getInstance("SHA-256"), is);
        } catch (Exception exception) {
            log.error("Error calculateHash", exception);
            return null;
        }
    }
    public static String calculateCloseable(  MessageDigest digest,InputStream is) {
        try {
            try (is) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }
            return bytesToHex(digest.digest());

        } catch (Exception exception) {
            log.error("Error calculateHash", exception);
            return null;
        }
    }

    // 将字节数组转换为十六进制字符串
    public static String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

}

