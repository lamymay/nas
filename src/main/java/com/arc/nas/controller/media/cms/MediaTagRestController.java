package com.arc.nas.controller.media.cms;

import com.arc.nas.model.domain.system.common.FileTag;
import com.arc.nas.model.request.MediaTagRequest;
import com.arc.nas.model.request.app.media.BatchItemResult;
import com.arc.nas.repository.dao.system.FileTagDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 媒体管理（CMS）：内容打标相关
 */
@RestController
@RequestMapping("/media/cms/tag")
public class MediaTagRestController {

    private static final Logger log = LoggerFactory.getLogger(MediaTagRestController.class);

    private final FileTagDAO fileTagDAO;

    public MediaTagRestController(FileTagDAO fileTagDAO) {
        this.fileTagDAO = fileTagDAO;
    }


    @PostMapping("/saves")
    public ResponseEntity<Boolean> saves(@RequestBody List<FileTag> records) {
        try {
            return ResponseEntity.ok(fileTagDAO.saveAll(records));
        } catch (Exception e) {
            log.error("扫描任务异常", e);
            return ResponseEntity.status(500).body(false);
        }
    }

    @GetMapping("/removes/{code}")
    public ResponseEntity<Map<String, BatchItemResult>> remove(@PathVariable String code) {
        return ResponseEntity.ok(fileTagDAO.deleteByCodes(List.of(code.split(","))));
    }

    @PostMapping("/updates/{code}")
    public ResponseEntity<Boolean> updateAll(@RequestBody List<FileTag> records) {
        return ResponseEntity.ok(fileTagDAO.updateAll(records));
    }

    @PostMapping("/list")
    public ResponseEntity<List<FileTag>> list(@RequestBody MediaTagRequest request) {
        return ResponseEntity.ok(fileTagDAO.list(request));
    }


}
