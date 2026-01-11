package com.arc.util.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FFmpegThumbnailGenerator {
    private static final Logger log = LoggerFactory.getLogger(FFmpegThumbnailGenerator.class);

    /**
     * 格式化时间点为文件可用的字符串，如 "00:02:15" -> "00-02-15"
     */
    private static String formatTimeForFilename(String timePoint) {
        return timePoint.replace(":", "-");
    }

    /**
     * 生成单张视频缩略图
     */
    public static File generateVideoThumbnail(File videoFile, File outputDir, String timePoint, int width, boolean overwrite)
            throws IOException, InterruptedException {

        if (!outputDir.exists()) outputDir.mkdirs();

        String baseName = videoFile.getName();
        if (baseName.contains(".")) {
            baseName = baseName.substring(0, baseName.lastIndexOf('.'));
        }

        String timeStr = formatTimeForFilename(timePoint);
        File thumbnailFile = new File(outputDir, baseName + "_" + timeStr + "_01.jpg");

        if (!overwrite && thumbnailFile.exists()) {
            return thumbnailFile;
        }

        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-ss");
        command.add(timePoint);
        command.add("-i");
        command.add(videoFile.getAbsolutePath());
        command.add("-vframes");
        command.add("1");
        command.add("-q:v");
        command.add("2");
        // 色彩空间修复 + 等比例缩放
        command.add("-vf");
        command.add("scale=" + width + ":-1,format=yuvj420p");
        command.add(thumbnailFile.getAbsolutePath());
        command.add("-y");

        executeCommand(command);

        return thumbnailFile;
    }

    /**
     * 生成多张视频缩略图
     */
    public static List<File> generateVideoThumbnails(String inputVideoFile, String outputFolder,
                                                     List<String> timePoints, int width,
                                                     boolean overwrite) {

        File outputDir = new File(outputFolder);
        File videoFile = new File(inputVideoFile);
        if (!outputDir.exists()) outputDir.mkdirs();

        String baseName = videoFile.getName();
        if (baseName.contains(".")) {
            baseName = baseName.substring(0, baseName.lastIndexOf('.'));
        }

        List<File> thumbnails = new ArrayList<>();

        for (String timePoint : timePoints) {
            String timeStr = formatTimeForFilename(timePoint);
            // 循环生成截图时加序号
            int seq = 1;
            File thumbFile;
            do {
                thumbFile = new File(outputDir, String.format("%s_%s_%02d.jpg", baseName, timeStr, seq++));
            } while (!overwrite && thumbFile.exists());

            thumbnails.add(thumbFile);

            List<String> command = new ArrayList<>();
            command.add("ffmpeg");
            command.add("-ss");
            command.add(timePoint);
            command.add("-i");
            command.add(videoFile.getAbsolutePath());
            command.add("-vframes");
            command.add("1");
            command.add("-q:v");
            command.add("2");
            // 色彩空间兼容 + 等比例缩放
            command.add("-vf");
            command.add("scale=" + width + ":-1,format=yuvj420p");
            command.add(thumbFile.getAbsolutePath());
            command.add("-y");

            try {
                executeCommand(command);
            } catch (Exception e) {
                log.info("error generateVideoThumbnails input=" + inputVideoFile, e);
            }
        }

        return thumbnails;
    }

    /**
     * 对图片生成缩略图并转换格式
     */
    public static File generateImageThumbnail(File imageFile, File outputDir,
                                              int width, boolean overwrite, String format) {

        if (!outputDir.exists()) outputDir.mkdirs();

        String baseName = imageFile.getName();
        if (baseName.contains(".")) {
            baseName = baseName.substring(0, baseName.lastIndexOf('.'));
        }

        format = format.toLowerCase();
        File thumbnailFile = new File(outputDir, baseName + "_thumbnail." + format);

        if (!overwrite && thumbnailFile.exists()) {
            return thumbnailFile;
        }

        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-i");
        command.add(imageFile.getAbsolutePath());
        // 色彩空间兼容 + 等比例缩放
        command.add("-vf");
        command.add("scale=" + width + ":-1,format=yuvj420p");
        command.add(thumbnailFile.getAbsolutePath());
        command.add("-y");

        try {
            executeCommand(command);
        } catch (Exception e) {
            log.info("error generateImageThumbnail input=" + imageFile, e);
        }

        return thumbnailFile;
    }

    /**
     * 执行命令并输出日志
     */
    private static void executeCommand(List<String> command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        Process process = pb.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg执行失败，退出码：" + exitCode);
        }
    }
}
