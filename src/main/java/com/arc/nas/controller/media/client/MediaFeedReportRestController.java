package com.arc.nas.controller.media.client;

import com.arc.nas.model.domain.app.media.MediaClientViewLog;
import com.arc.nas.repository.mysql.dao.system.MediaClientViewLogDAO;
import com.arc.nas.service.app.media.impl.MediaFeedService;
import com.arc.nas.service.app.media.impl.UrlHelper;
import com.arc.nas.service.system.common.SysFileService;
import com.arc.util.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 模拟抖音视频流 播放进度上报接口
 */
@RestController
@RequestMapping("/feed")
public class MediaFeedReportRestController {

    private static final Logger log = LoggerFactory.getLogger(MediaFeedReportRestController.class);

    private final MediaFeedService mediaFeedService;
    private final SysFileService sysFileService;
    private final UrlHelper urlHelper;
    private final MediaClientViewLogDAO mediaClientViewLogDAO;

    public MediaFeedReportRestController(MediaFeedService mediaFeedService, SysFileService sysFileService,
                                         UrlHelper urlHelper,
                                         MediaClientViewLogDAO mediaClientViewLogDAO
    ) {
        this.mediaFeedService = mediaFeedService;
        this.sysFileService = sysFileService;
        this.urlHelper = urlHelper;
        this.mediaClientViewLogDAO = mediaClientViewLogDAO;
    }

    @PostMapping("/report")
    public ResponseEntity<MediaClientViewLog> report(@RequestBody MediaClientViewLog viewLog) {
        // todo 后续异步处理逻辑，比如 给推送系统计算权重
        if (viewLog.getClientCode() == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            log.info("report viewLog={} ", JSON.toJSONString(viewLog));
            return ResponseEntity.ok(mediaClientViewLogDAO.saveOrUpdateOne(viewLog));
        } catch (Exception exception) {
            log.error("report", exception);
            return ResponseEntity.badRequest().build();

        }
    }

}
