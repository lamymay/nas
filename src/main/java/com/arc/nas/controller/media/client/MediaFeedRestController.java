package com.arc.nas.controller.media.client;

import com.arc.nas.model.dto.app.media.MediaFeedDTO;
import com.arc.nas.model.request.app.media.FeedQuery;
import com.arc.nas.service.app.media.impl.MediaFeedService;
import com.arc.nas.service.app.media.impl.UrlHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 模拟抖音视频流 首页
 */
@RestController
@RequestMapping("/feed")
public class MediaFeedRestController {

    private static final Logger log = LoggerFactory.getLogger(MediaFeedRestController.class);

    private final MediaFeedService mediaFeedService;

    private final UrlHelper urlHelper;

    public MediaFeedRestController(MediaFeedService mediaFeedService, UrlHelper urlHelper) {
        this.mediaFeedService = mediaFeedService;
        this.urlHelper = urlHelper;
    }

    /**
     * @param cursor      游标位置，当前页面资源的开始位置（即列表的第一个数据的id）
     * @param step        步长 正数往未来翻页/负数往较旧数据翻页， 缺省值为 20000
     * @param contentType 视频/图片/音乐 或者其他扩展  建议采用分类加权重设计
     * @param deviceId    用户唯一性标记
     * @param deviceType  设备类型 保留，暂时不使用
     * @param keyword     搜索时候的关键词
     * @return MediaPageDTO
     */
    @GetMapping({"", "/home"})
    public ResponseEntity<MediaFeedDTO> feed(
            @RequestParam(defaultValue = "1") Integer cursor,
            @RequestParam(defaultValue = "20000") Integer step,
            @RequestParam(required = false) String contentType,
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) String deviceType,
            @RequestParam(required = false) String keyword
    ) {
        FeedQuery query = new FeedQuery();
        query.setCursor(cursor);
        query.setStep(step);
        query.setContentType(contentType);
        query.setDeviceId(deviceId);
        query.setDeviceType(deviceType);
        query.setKeyword(keyword);
        return ResponseEntity.ok(mediaFeedService.feed(query));
    }
}
