package com.arc.util;

import com.arc.util.file.FileUtil;

import java.util.HashMap;
import java.util.Map;

public class MimeTypeTool {

    private static final Map<String, String> EXTENSION_TO_MIME;
    private static final String STREAM = "application/octet-stream";

    static {
        EXTENSION_TO_MIME = new HashMap<>();
        // 视频
        EXTENSION_TO_MIME.put("mp4", "video/mp4");
        EXTENSION_TO_MIME.put("mkv", "video/x-matroska");
        EXTENSION_TO_MIME.put("avi", "video/x-msvideo");
        EXTENSION_TO_MIME.put("mov", "video/quicktime");
        EXTENSION_TO_MIME.put("flv", "video/x-flv");
        EXTENSION_TO_MIME.put("wmv", "video/x-ms-wmv");
        // 音频
        EXTENSION_TO_MIME.put("mp3", "audio/mpeg");
        EXTENSION_TO_MIME.put("wav", "audio/wav");
        EXTENSION_TO_MIME.put("flac", "audio/flac");
        EXTENSION_TO_MIME.put("aac", "audio/aac");
        EXTENSION_TO_MIME.put("ogg", "audio/ogg");
        // 图片
        EXTENSION_TO_MIME.put("jpg", "image/jpeg");
        EXTENSION_TO_MIME.put("jpeg", "image/jpeg");
        EXTENSION_TO_MIME.put("png", "image/png");
        EXTENSION_TO_MIME.put("gif", "image/gif");
        EXTENSION_TO_MIME.put("bmp", "image/bmp");
        EXTENSION_TO_MIME.put("webp", "image/webp");
        // 文档
        EXTENSION_TO_MIME.put("pdf", "application/pdf");
        EXTENSION_TO_MIME.put("txt", "text/plain");
        EXTENSION_TO_MIME.put("csv", "text/csv");
        EXTENSION_TO_MIME.put("doc", "application/msword");
        EXTENSION_TO_MIME.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        EXTENSION_TO_MIME.put("xls", "application/vnd.ms-excel");
        EXTENSION_TO_MIME.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        EXTENSION_TO_MIME.put("ppt", "application/vnd.ms-powerpoint");
        EXTENSION_TO_MIME.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        // 默认
        EXTENSION_TO_MIME.put("default", STREAM);//"application/octet-stream"
    }


    /**
     * 根据文件扩展名返回 MIME 类型
     *
     * @param extension 文件扩展名（不带点），小写优先
     * @return 标准 MIME 类型
     */
    public static String getMimeType(String extension) {
        if (extension == null || extension.isEmpty()) {
            return STREAM;
        }
        extension = extension.toLowerCase();
        return EXTENSION_TO_MIME.getOrDefault(extension, STREAM);
    }

    public static String getMimeTypeAuto(String fileName) {
        if (fileName == null) return STREAM;
        String extension = FileUtil.getExtension(fileName);
        if (extension == null) return STREAM;
        return getMimeType(extension);
    }
}
