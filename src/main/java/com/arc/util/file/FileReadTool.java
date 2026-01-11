package com.arc.util.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class FileReadTool {

    private static final Logger log = LoggerFactory.getLogger(FileReadTool.class);
    static List<Path> iso8859_1_files = new LinkedList<>();

    /**
     * Files.lines(filePath) 返回的 Stream<String> 是通过底层的缓冲和 IO 操作一次性读取整个文件内容的。这种方式相对更为高效，因为它可以利用缓冲机制和更少的 IO 操作来处理文件的读取。
     *
     * @param source              source
     * @param showDebugLineNumber showDebugLineNumber
     * @return String
     */
    @Deprecated
    public static String readLinesAsStringV0(File source, boolean showDebugLineNumber) {
        String temp;
        StringBuilder lines = new StringBuilder();
        BufferedReader reader = null;

        int line = 0;
        try {
            reader = new BufferedReader(new FileReader(source));
            while ((temp = reader.readLine()) != null) {
                lines.append(temp);
                lines.append("\n");
                line++;
                if (showDebugLineNumber) {
                    System.out.println(line + " " + temp);
                }
            }
        } catch (Exception exception) {
            log.error("readLinesAsString", exception);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception exception) {
                    log.error("readLinesAsString reader.close() error", exception);
                }
            }
        }
        String linesString = lines.toString();
        return (linesString.length() > 1) ? linesString.substring(0, linesString.length() - 1) : linesString;

    }

    public static String readLinesAsStringV1(File source, boolean showDebugLineNumber) {
        StringBuilder lines = new StringBuilder();
        int line = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(source))) {
            String temp;
            while ((temp = reader.readLine()) != null) {
                lines.append(temp).append("\n");
                line++;
                if (showDebugLineNumber) {
                    System.out.println(line + " " + temp);
                }
            }
        } catch (IOException exception) {
            log.error("readLinesAsString", exception);
        }
        String linesString = lines.toString();
        return (linesString.length() > 1) ? linesString.substring(0, linesString.length() - 1) : linesString;

    }

    // 优化后的读取文件内容为字符串的方法，使用 java.nio.file.Files
    public static String readLinesAsStringV3(Path filePath, boolean showDebugLineNumber) {
        StringBuilder lines = new StringBuilder();
        AtomicInteger line = new AtomicInteger(1);

        try {
            Files.lines(filePath).forEachOrdered(temp -> {
                lines.append(temp).append("\n");
                line.getAndIncrement();
                if (showDebugLineNumber) {
                    System.out.println(line + " " + temp);
                    log.info(line + " " + temp);
                }
            });
        } catch (IOException exception) {
            log.error("readLinesAsString", exception);
        }

        return lines.toString();
    }

    public static String readLinesAsString(Path filePath, boolean showDebugLineNumber) {

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            log.warn("文件不存在或者不是是普通文件,file=" + filePath);
            return null;
        }

        try {
            return readLinesAsString(filePath, StandardCharsets.UTF_8, showDebugLineNumber);

        } catch (Exception exception) {
            log.warn("Error readLinesAsString over UTF-8 ,so will try GBK. filePath=" + filePath, exception);
            try {
                return readLinesAsString(filePath, StandardCharsets.ISO_8859_1, showDebugLineNumber);
            } catch (IOException e) {
                log.error("Error readLinesAsString over ISO_8859_1. filePath=" + filePath, exception);
                throw new RuntimeException(e);
            }
        }

    }

    public static String readLinesAsString(Path filePath, Charset charset, boolean showDebugLineNumber) throws IOException {
        AtomicInteger line = new AtomicInteger(1);
        if (showDebugLineNumber) {
            return Files.lines(filePath, charset).map(temp -> {
                log.info(line + " " + temp);
                return temp;
            }).collect(Collectors.joining("\n"));
        } else {
            // 读取所有行
            return String.join("\n", Files.readAllLines(filePath, charset));
        }

    }

    public static String convert_file_ISO8859_1_to_UTF8(Path filePath) {

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            log.warn("文件不存在或者不是是普通文件,file=" + filePath);
            return null;
        }

        try {
            return readLinesAsString(filePath, StandardCharsets.UTF_8, false);

        } catch (Exception exception) {
            log.warn("Error readLinesAsString over UTF-8 ,so will try GBK. filePath=" + filePath, exception);
            try {
                String contents = readLinesAsString(filePath, StandardCharsets.ISO_8859_1, false);
                File file = new File(filePath.toFile().getAbsolutePath());
                iso8859_1_files.add(filePath);
                return contents;
            } catch (IOException e) {
                log.error("Error readLinesAsString over ISO_8859_1. filePath=" + filePath, exception);
                throw new RuntimeException(e);
            }
        }

    }

}
