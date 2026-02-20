package com.arc.nas.controller.media.client;

import com.arc.nas.model.domain.system.common.SysFile;
import com.arc.nas.service.app.media.MediaService;
import com.arc.nas.service.app.media.impl.UrlHelper;
import com.arc.nas.service.system.common.SysFileService;
import com.arc.util.LimitedInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.arc.util.MimeTypeTool.getMimeTypeAuto;


/**
 * response binary「响应文件的数据流」
 */
@RestController
public class MediaBinaryRestController {

    private static final Logger log = LoggerFactory.getLogger(MediaBinaryRestController.class);

    private final MediaService mediaService;
    private final SysFileService sysFileService;

    private final UrlHelper urlHelper;

    public MediaBinaryRestController(MediaService mediaService, SysFileService sysFileService, UrlHelper urlHelper) {
        this.mediaService = mediaService;
        this.sysFileService = sysFileService;
        this.urlHelper = urlHelper;
    }

    @GetMapping("/{code}")
    public ResponseEntity<Resource> getMediaFile(
            @PathVariable String code,
            @RequestHeader(value = "Range", required = false) String rangeHeader) throws IOException {

        SysFile sysFile = sysFileService.getByIdOrCode(code);
        if (sysFile == null) return ResponseEntity.notFound().build();

        File file = new File(sysFile.getPath());
        if (!file.exists() || !file.isFile()) return ResponseEntity.notFound().build();

        // 1. 统一使用 FileSystemResource，Spring 会自动处理分段逻辑
        FileSystemResource resource = new FileSystemResource(file);
        String mimeType = getMimeTypeAuto(sysFile.getOriginalName());
        MediaType mediaType = (mimeType != null) ? MediaType.parseMediaType(mimeType) : MediaType.APPLICATION_OCTET_STREAM;

        // 2. 如果没有 Range 请求（比如图片），直接返回全量数据
        if (rangeHeader == null) {
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .contentLength(file.length())
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.inline().filename(sysFile.getOriginalName(), StandardCharsets.UTF_8).build().toString())
                    .body(resource);
        }

        // 3. 处理视频分段请求 (Range)
        // 利用 Spring 自带的 HttpRange 工具类，更健壮
        List<HttpRange> httpRanges = HttpRange.parseRanges(rangeHeader);
        if (httpRanges.isEmpty()) {
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).build();
        }

        // 为了简化处理，通常取第一个 Range 即可
        HttpRange range = httpRanges.get(0);
        long start = range.getRangeStart(file.length());
        long end = range.getRangeEnd(file.length());
        long contentLength = end - start + 1;

        // 重点：Spring 对 ResourceRegion 的支持非常完美，能解决大文件播放问题
        // 如果你不想写繁琐的 headers，可以直接让 Spring 处理这部分逻辑：
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(mediaType)
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + file.length())
                .contentLength(contentLength)
                .body(new HttpRegionResource(resource, start, contentLength));
    }

    /**
     * 内部类辅助 Spring 进行分段输出，避免手动控制流
     */
    private static class HttpRegionResource extends InputStreamResource {
        private final Resource resource;
        private final long start;
        private final long length;

        public HttpRegionResource(Resource resource, long start, long length) throws IOException {
            super(resource.getInputStream()); // 占位，实际会被重写
            this.resource = resource;
            this.start = start;
            this.length = length;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            InputStream is = resource.getInputStream();
            is.skip(start);
            return new LimitedInputStream(is, length);
        }
    }


}
