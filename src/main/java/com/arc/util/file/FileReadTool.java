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
import java.util.TreeSet;
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
//                FileUtil.deleteFile(file, true);
//                FileUtil.requireFileDirectoryExistsOrElseTryCreate(file);
//                FileUtil.writeToDisk(contents, file.getAbsolutePath(), true);
                iso8859_1_files.add(filePath);
                return contents;
            } catch (IOException e) {
                log.error("Error readLinesAsString over ISO_8859_1. filePath=" + filePath, exception);
                throw new RuntimeException(e);
            }
        }

    }


    public static TreeSet<String> readLines(File source) {
        BufferedReader reader = null;
        String temp = null;
        TreeSet<String> lines = new TreeSet<>();
        int line = 1;
        try {
            reader = new BufferedReader(new FileReader(source));
            while ((temp = reader.readLine()) != null) {
                lines.add(temp);
                line++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return lines;
    }


    ///

    private static void testReadFunV2() {

//        List<File> files = FileUtil.listFileByFolder("E:\\syncd");
        List<File> files = FileUtil.listFileByFolder("E:\\syncd\\encoding");

        for (File file : files) {
            if (FileUtil.isTxt(file)) {
                System.out.println(convert_file_ISO8859_1_to_UTF8(file.toPath()));
            }
        }

        iso8859_1_files.stream().forEach(System.out::println);
//        String content1 = readLinesAsString(new File("E:\\syncd\\HR\\__all\\春.txt").toPath(), false);
//        String content2 = readLinesAsString(new File("E:\\syncd\\HR\\__all\\大学英语精读答案.txt").toPath(), false);
//        String content3 = readLinesAsString(new File("E:\\syncd\\HR\\__all\\ng.txt").toPath(), false);
//
//        System.out.println(content1);
//        System.out.println(content2);
//        System.out.println(content3);

    }

    private static void testReadFun() {
        File sourceFile = new File("H:\\code\\java\\fx\\doc\\simple\\A\\测试JS.js");
        String content1 = readLinesAsStringV1(sourceFile, false);
        String content2 = readLinesAsString(sourceFile.toPath(), false);
        System.out.println("------readLinesAsStringV1 ------");
        System.out.println(content1);
        System.out.println("------ readLinesAsString ------");
        System.out.println(content2);
        System.out.println("------ equals? ------");
        System.out.println(content1.equals(content2));

        System.out.println(content1.length());
        System.out.println(content2.length());

    }


    public static void main(String[] args) {
        // 示例用法
        //testReadFun();
        TreeSet<String> lines = FileUtil.readLines(new File("/Users/may/Desktop/code/java/1/zero/log/2.txt"));
        TreeSet<String> rows = new TreeSet<>();
        for (String item : lines) {
            if (item.startsWith("#")) continue;
            if (item.endsWith("/")) {
                item = item.substring(0, item.length() - 1);
            }
            rows.add(item);
        }

        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();

        for (String item : rows) {
            System.out.println(item);
        }

        System.out.println();
        System.out.println();
        System.out.println(lines.size() + " / " + rows.size());

    }

}
