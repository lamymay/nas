package com.arc.nas.controller.data.app.media;

import com.arc.util.LimitedInputStream;
import com.arc.util.JSON;
import com.arc.nas.model.domain.system.common.SysFile;
import com.arc.nas.model.dto.app.media.MediaItemDTO;
import com.arc.nas.model.dto.app.media.MediaPageDTO;
import com.arc.nas.model.dto.app.media.ScanRequest;
import com.arc.nas.model.request.app.media.SysFilePageable;
import com.arc.nas.model.request.app.media.SysFileQuery;
import com.arc.nas.model.request.app.media.*;
import com.arc.nas.service.app.media.MediaService;
import com.arc.nas.service.app.media.impl.UrlHelper;
import com.arc.nas.service.system.common.SysFileService;
import com.arc.util.StringTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        if (sysFile == null) {
            return ResponseEntity.notFound().build();
        }

        File file = new File(sysFile.getPath());
        if (!file.exists() || !file.isFile()) {
            return ResponseEntity.notFound().build();
        }

        String mimeType = sysFile.getMimeType();
        MediaType mediaType = (mimeType != null) ? MediaType.parseMediaType(mimeType) : MediaType.APPLICATION_OCTET_STREAM;
        long fileLength = file.length();
        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            // 处理 Range 请求
            String[] ranges = rangeHeader.substring(6).split("-");
            long start = 0;
            long end = fileLength - 1;

            try {
                start = Long.parseLong(ranges[0]);
                if (ranges.length > 1 && !ranges[1].isEmpty()) {
                    end = Long.parseLong(ranges[1]);
                }
            } catch (NumberFormatException e) {
                start = 0;
                end = fileLength - 1;
            }

            if (end >= fileLength) {
                end = fileLength - 1;
            }

            long contentLength = end - start + 1;

            InputStream inputStream = new FileInputStream(file);
            inputStream.skip(start);

            InputStreamResource resource = new InputStreamResource(new LimitedInputStream(inputStream, contentLength));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            headers.setContentLength(contentLength);
            headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
            headers.set(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileLength);

            return new ResponseEntity<>(resource, headers, HttpStatus.PARTIAL_CONTENT);
        }

        // return full file
        mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (sysFile.getMimeType() != null) {
            mediaType = MediaType.parseMediaType(sysFile.getMimeType());
        }

        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        String filename = sysFile.getOriginalName();
        ContentDisposition contentDisposition = ContentDisposition.inline().filename(filename, StandardCharsets.UTF_8).build();

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString()).contentLength(file.length()).contentType(mediaType).body(resource);
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
    public ResponseEntity<CleanThumbnailsResult> cleanThumbnails(@RequestParam(required = false) boolean moveToTrash) {
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
        Map<String, Map<String, SysFile>> byMediaTypesMap = sysFileService.listAllByQuery(query);
        Map<String, Map<String, MediaItemDTO>> finalMap = new HashMap<>();
        for (Map.Entry<String, Map<String, SysFile>> stringMapEntry : byMediaTypesMap.entrySet()) {
            String mediaTypeKey = stringMapEntry.getKey();
            Map<String, SysFile> sysFileMap = stringMapEntry.getValue();
            List<SysFile> files = sysFileMap.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
            Map<String, MediaItemDTO> collect = urlHelper.covertSysFileToMediaItemDTO(files).stream().collect(Collectors.toMap(MediaItemDTO::getCode, f -> f));
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
