package com.arc.nas.controller.media.cms;

import com.arc.nas.model.domain.app.media.MediaFileResource;
import com.arc.nas.model.dto.app.media.ScanRequest;
import com.arc.nas.service.app.media.MediaResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 媒体管理（CMS）：配置扫描文件夹相关
 */
@RestController
@RequestMapping("/media/cms/resource")
public class MediaResourceRestController {

    private static final Logger log = LoggerFactory.getLogger(MediaResourceRestController.class);

    private final MediaResource mediaResource;

    public MediaResourceRestController(
            MediaResource mediaResource) {
        this.mediaResource = mediaResource;
    }

    @GetMapping("/listAll")
    public ResponseEntity<List<MediaFileResource>> listAll() {
        return ResponseEntity.ok(mediaResource.listAll());
    }

    @PostMapping("/saves")
    public ResponseEntity<List<MediaFileResource>> saveAll(@RequestBody ScanRequest req) {
        String[] folders = req.getFolders();
        log.info("收到扫描请求, folders={}", String.join(",", folders));
        return ResponseEntity.ok(mediaResource.saveAll(folders));
    }

}
