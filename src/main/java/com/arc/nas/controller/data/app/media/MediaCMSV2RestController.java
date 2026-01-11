package com.arc.nas.controller.data.app.media;

import com.arc.nas.model.domain.app.media.MediaFileResource;
import com.arc.nas.model.dto.app.media.ScanRequest;
import com.arc.nas.service.app.media.MediaResource;
import com.arc.nas.service.app.media.impl.UrlHelper;
import com.arc.nas.service.system.common.SysFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/media")
public class MediaCMSV2RestController {

    private static final Logger log = LoggerFactory.getLogger(MediaCMSV2RestController.class);

    private final UrlHelper urlHelper;

    private final SysFileService sysFileService;
    private final MediaResource mediaResource;

    public MediaCMSV2RestController(SysFileService sysFileService,
                                    UrlHelper urlHelper,
                                    MediaResource mediaResource) {
        this.sysFileService = sysFileService;
        this.urlHelper = urlHelper;
        this.mediaResource = mediaResource;

    }

    /**
     * 设定 待扫描文件夹s
     *
     * @param req 文件夹路径数组
     * @return true=扫描成功，false=部分或全部失败
     */
    @PostMapping("/scan")
    public ResponseEntity<Object> scan(@RequestBody ScanRequest req) {
        String[] folders = req.getFolders();
        log.info("收到扫描请求, folders={}", String.join(",", folders));
        try {
            return ResponseEntity.ok(mediaResource.setScan(req));
        } catch (Exception e) {
            log.error("扫描任务异常", e);
            return ResponseEntity.ok(0);
        }
    }

    @PostMapping("/resource/saves")
    public ResponseEntity<List<MediaFileResource>> saveAll(@RequestBody ScanRequest req) {
        String[] folders = req.getFolders();
        log.info("收到扫描请求, folders={}", String.join(",", folders));
        return ResponseEntity.ok(mediaResource.saveAll(folders));
    }

    @GetMapping("/resource/listAll")
    public ResponseEntity<List<MediaFileResource>> listAll(  ) {
        return ResponseEntity.ok(mediaResource.listAll());

    }

    @PostMapping("/resource/removes")
    public ResponseEntity<Integer> removeAll(@RequestBody(required = false) ScanRequest req) {
        if (req == null) {
            return ResponseEntity.ok(mediaResource.deleteAll());
        } else {
            return ResponseEntity.ok(mediaResource.deleteAll(req.getFolders()));

        }
    }


}
