package com.arc.nas.controller.media.cms;

import com.arc.nas.model.domain.system.common.SysFile;
import com.arc.nas.model.dto.app.media.MediaItemDTO;
import com.arc.nas.model.dto.app.media.ScanRequest;
import com.arc.nas.model.request.app.media.*;
import com.arc.nas.service.app.media.MediaResource;
import com.arc.nas.service.app.media.impl.UrlHelper;
import com.arc.nas.service.system.common.SysFileService;
import com.arc.nas.service.system.common.impl.MQMocker;
import com.arc.util.JSON;
import com.arc.util.StringTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.arc.nas.model.dto.app.media.MediaItemDTO.covertSysFileToMediaItemDTO;
import static com.arc.nas.service.system.common.SysFileService.VIDEO;

/**
 * 媒体管理（CMS）：内容管理
 */
@RestController
@RequestMapping("/media/cms")
public class MediaCMSRestController {

    private static final Logger log = LoggerFactory.getLogger(MediaCMSRestController.class);

    private final UrlHelper urlHelper;
    private final MediaResource mediaResource;
    private final SysFileService sysFileService;
    private final MQMocker mqMocker;

    public MediaCMSRestController(
            UrlHelper urlHelper,
            MediaResource mediaResource,
            SysFileService sysFileService,
            MQMocker mqMocker) {
        this.urlHelper = urlHelper;
        this.mediaResource = mediaResource;
        this.sysFileService = sysFileService;
        this.mqMocker = mqMocker;

    }

    /**
     * 扫描指定文件夹，将文件同步到数据库
     *
     * @param req 文件夹路径数组
     * @return true=扫描成功，false=部分或全部失败
     */
    @PostMapping("/scan")
    public ResponseEntity<Object> scan(@RequestBody ScanRequest req) {
        String[] folders = req.getFolders();
        log.info("收到扫描请求, folders={}", String.join(",", folders));
        try {
            return ResponseEntity.ok(mediaResource.scan(folders));
        } catch (Exception e) {
            log.error("扫描任务异常", e);
            return ResponseEntity.ok(0);
        }
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

    @GetMapping("/remove/{code}")
    public ResponseEntity<Boolean> remove(@PathVariable String code) {
        return ResponseEntity.ok(sysFileService.deleteByCode(code));
    }

    @GetMapping("/removes/{codes}")
    public ResponseEntity<Map<String, BatchItemResult>> removes(@PathVariable String codes) {
        return ResponseEntity.ok(sysFileService.deleteByCodes(codes.split(",")));
    }

    @PostMapping("/updates")
    public ResponseEntity<BatchResult> updateAll(@RequestBody List<SysFile> records) {
        log.info("updateAll={}", JSON.toJSONString(records));
        return ResponseEntity.ok(sysFileService.updateAllByCodes(records));
    }

    @GetMapping(value = "/cleanThumbnails")
    public ResponseEntity<CleanThumbnailsResult> cleanThumbnails(@RequestParam(required = false, defaultValue = "true") boolean moveToTrash) {
        return ResponseEntity.ok(mediaResource.cleanThumbnails(moveToTrash));
    }

    // 刷新缩略图的
    @RequestMapping(value = "/matchThumbnails", method = {RequestMethod.POST})
    public ResponseEntity<GenerateThumbnailResult> autoMatchThumbnails(@RequestBody GenerateThumbnailConfig config) {

        long t1 = System.currentTimeMillis();
        log.info("autoMatchThumbnails收到扫描请求");
        mediaResource.autoMatchThumbnails();
        long t2 = System.currentTimeMillis();
        String autoMatchThumbnailCost = StringTool.displayTimeWithUnit(t1, t2);
        log.info("autoMatchThumbnails 扫描请求END 耗时={}", autoMatchThumbnailCost);
        final GenerateThumbnailResult generateThumbnailResult;
        if (config.isForce()) {
            generateThumbnailResult = mediaResource.generateThumbnails(config);
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

    ////
    @PostMapping("/addTag")
    public ResponseEntity<BatchResult> addTag(@RequestBody AddTagInner addTagInner) {
        log.info("updateAll={}", JSON.toJSONString(addTagInner));
        return ResponseEntity.ok(mediaResource.addTag(addTagInner));
    }

    @PostMapping("/removeTags")
    public ResponseEntity<BatchResult> removeTags(@RequestBody RemoveTagBatchRequest request) {
        log.info("removeTags={}", JSON.toJSONString(request));
        return ResponseEntity.ok(mediaResource.removeTags(request));
    }

    // debug 开发中调试用的
    @PostMapping("/removes")
    public ResponseEntity<Integer> removeAll(@RequestBody(required = false) ScanRequest req) {
        if (req == null) {
            return ResponseEntity.ok(mediaResource.deleteAll());
        } else {
            return ResponseEntity.ok(mediaResource.deleteAll(req.getFolders()));

        }
    }

}
