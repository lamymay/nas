package com.arc.nas.controller.data.app.media;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
public class VideoController {

    private final ResourceLoader resourceLoader;

    public VideoController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @GetMapping("/video")
    public ResponseEntity<byte[]> getVideo(HttpHeaders headers) throws IOException {
        // 这里假设视频存储在 resources 目录下
        Resource videoResource = resourceLoader.getResource("classpath:video/sample.mp4");
        Path videoPath = videoResource.getFile().toPath();

        long fileLength = Files.size(videoPath);

        // 获取 Range 请求头
        String range = headers.getFirst("Range");
        long start = 0;
        long end = fileLength - 1;

        if (range != null && range.startsWith("bytes=")) {
            String[] ranges = range.substring(6).split("-");
            try {
                start = Long.parseLong(ranges[0]);
                if (ranges.length > 1) {
                    end = Long.parseLong(ranges[1]);
                }
            } catch (NumberFormatException e) {
                start = 0;
                end = fileLength - 1;
            }
        }

        if (start > end) {
            start = 0;
            end = fileLength - 1;
        }

        long contentLength = end - start + 1;
        byte[] data = new byte[(int) contentLength];

        try (InputStream inputStream = Files.newInputStream(videoPath)) {
            inputStream.skip(start);
            inputStream.read(data, 0, (int) contentLength);
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "video/mp4");
        responseHeaders.add("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
        responseHeaders.add("Accept-Ranges", "bytes");

        return ResponseEntity.status(206).headers(responseHeaders).body(data);
    }
}
