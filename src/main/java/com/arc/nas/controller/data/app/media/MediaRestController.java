package com.arc.nas.controller.data.app.media;

import com.arc.nas.model.domain.system.common.SysFile;
import com.arc.nas.model.dto.app.media.MediaItemDTO;
import com.arc.nas.model.dto.app.media.MediaPageDTO;
import com.arc.nas.model.dto.app.media.ScanRequest;
import com.arc.nas.model.request.app.media.*;
import com.arc.nas.service.app.media.MediaService;
import com.arc.nas.service.app.media.impl.UrlHelper;
import com.arc.nas.service.system.common.SysFileService;
import com.arc.util.JSON;
import com.arc.util.LimitedInputStream;
import com.arc.util.StringTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.arc.nas.model.dto.app.media.MediaItemDTO.covertSysFileToMediaItemDTO;
import static com.arc.nas.service.system.common.SysFileService.VIDEO;

@RestController
@RequestMapping("/api/media")
public class MediaRestController {

    private static final Logger log = LoggerFactory.getLogger(MediaRestController.class);

    private final MediaService mediaService;
    private final SysFileService sysFileService;

    private final UrlHelper urlHelper;

    public MediaRestController(MediaService mediaService, SysFileService sysFileService, UrlHelper urlHelper) {
        this.mediaService = mediaService;
        this.sysFileService = sysFileService;
        this.urlHelper = urlHelper;
    }

    @GetMapping("/home")
    public ResponseEntity<MediaPageDTO> listPage(
            @RequestParam(defaultValue = "1") Integer pageNumber,
            @RequestParam(defaultValue = "20000") Integer pageSize,
            @RequestParam(required = false) String mediaType,
            @RequestParam(required = false) String orderField,
            @RequestParam(required = false) String orderDirection,
            @RequestParam(required = false) String keyword
    ) {
        SysFilePageable pageable = new SysFilePageable(pageNumber, pageSize);
        pageable.setMediaType(mediaType);
        pageable.setKeyword(keyword);
        pageable.setOrderField(orderField);
        pageable.setOrderDirection(orderDirection);
        return ResponseEntity.ok(mediaService.listPage(pageable));
    }

    @GetMapping("/remove/{code}")
    public ResponseEntity<Boolean> remove(@PathVariable String code) {
        return ResponseEntity.ok(sysFileService.deleteByCode(code));
    }

    @GetMapping("/removes/{code}")
    public ResponseEntity<Map<String, BatchItemResult>> removes(@PathVariable String... code) {
        return ResponseEntity.ok(sysFileService.deleteByCodes(code));
    }

    @PostMapping("/updates")
    public ResponseEntity<BatchResult> updateAll(@RequestBody List<SysFile> records) {
        log.info("updateAll={}", JSON.toJSONString(records));
        return ResponseEntity.ok(sysFileService.updateAllByCodes(records));
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
        String mimeType = sysFile.getMimeType();
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


    /// for CMS

    /**
     * 扫描指定文件夹，将文件同步到数据库
     * todo use db config the config paths
     *
     * @param req 文件夹路径数组
     * @return true=扫描成功，false=部分或全部失败
     */
    @PostMapping("/scan")
    public Integer scan(@RequestBody ScanRequest req) {
        String[] folders = req.getFolders();
        log.info("收到扫描请求, folders={}", String.join(",", folders));
        try {
            return mediaService.scan(folders);
        } catch (Exception e) {
            log.error("扫描任务异常", e);
            return 0;
        }
    }

    @GetMapping(value = "/cleanThumbnails")
    public ResponseEntity<CleanThumbnailsResult> cleanThumbnails(@RequestParam(required = false, defaultValue = "true") boolean moveToTrash) {
        return ResponseEntity.ok(mediaService.cleanThumbnails(moveToTrash));
    }

    // 刷新缩略图的
    @RequestMapping(value = "/matchThumbnails", method = {RequestMethod.POST})
    public ResponseEntity<GenerateThumbnailResult> autoMatchThumbnails(@RequestBody GenerateThumbnailConfig config) {

        long t1 = System.currentTimeMillis();
        log.info("autoMatchThumbnails收到扫描请求");
        mediaService.autoMatchThumbnails();
        long t2 = System.currentTimeMillis();
        String autoMatchThumbnailCost = StringTool.displayTimeWithUnit(t1, t2);
        log.info("autoMatchThumbnails 扫描请求END 耗时={}", autoMatchThumbnailCost);
        final GenerateThumbnailResult generateThumbnailResult;
        if (config.isForce()) {
            generateThumbnailResult = mediaService.generateThumbnails(config);
        } else {
            generateThumbnailResult = new GenerateThumbnailResult();
        }
        long t3 = System.currentTimeMillis();
        String generateThumbnailsCost = StringTool.displayTimeWithUnit(t3, t2);
        log.info("generateThumbnails 扫描请求END 耗时={}", generateThumbnailsCost);

        Map<String, Object> extendMap = new HashMap<>();
        extendMap.put("autoMatchThumbnails cost", autoMatchThumbnailCost);
        extendMap.put("generateThumbnails cost", generateThumbnailsCost);
        generateThumbnailResult.setExtendMap(extendMap);
        return ResponseEntity.ok(generateThumbnailResult);

    }

    @PostMapping("/list")
    public ResponseEntity<Map<String, Map<String, MediaItemDTO>>> listMedia(@RequestBody SysFileQuery query) {
        if (query.getMediaTypes() == null) {
            query.setMediaTypes(Arrays.asList(VIDEO, SysFileService.IMAGE).stream().collect(Collectors.toSet()));
        }
        log.info("listMedia query={}", JSON.toJSONString(query));
        Map<String, Map<String, SysFile>> byMediaTypesMap = sysFileService.listAsTypeMapAllByQuery(query);
        Map<String, Map<String, MediaItemDTO>> finalMap = new HashMap<>();
        for (Map.Entry<String, Map<String, SysFile>> stringMapEntry : byMediaTypesMap.entrySet()) {
            String mediaTypeKey = stringMapEntry.getKey();
            Map<String, SysFile> sysFileMap = stringMapEntry.getValue();
            List<SysFile> files = sysFileMap.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
            String prefix = urlHelper.getPrefix();
            Map<String, MediaItemDTO> collect = covertSysFileToMediaItemDTO(files, prefix).stream().collect(Collectors.toMap(MediaItemDTO::getCode, f -> f));
            finalMap.put(mediaTypeKey, collect);
        }
        return ResponseEntity.ok(finalMap);
    }


    @PostMapping("/addTag")
    public ResponseEntity<BatchResult> addTag(@RequestBody AddTagRequest addTagRequest) {
        log.info("updateAll={}", JSON.toJSONString(addTagRequest));
        return ResponseEntity.ok(mediaService.addTag(addTagRequest));
    }

    @PostMapping("/removeTags")
    public ResponseEntity<BatchResult> removeTags(@RequestBody RemoveTagBatchRequest request) {
        log.info("removeTags={}", JSON.toJSONString(request));
        return ResponseEntity.ok(mediaService.removeTags(request));
    }


}
