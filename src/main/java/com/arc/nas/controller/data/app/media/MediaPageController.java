package com.arc.nas.controller.data.app.media;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 处理返回html页面的Controller
 */
@Controller
public class MediaPageController {

    @GetMapping("/media/home")
    public String homeHome() {
        return "media/media_home";
    }

    @GetMapping("/media/video")
    public String media_video() {
        return "media/media_video";
    }

    @GetMapping("/media/image")
    public String mediaImage() {
        return "media/media_image";
    }

    @GetMapping("/media/cms")
    public String mediaCMS() {
        // 页面初始加载时不需要传数据，由前端通过接口动态请求
        return "media/media_cms";
    }

    /// 模仿抖音
    @GetMapping("/media/feed")
    public String media_video_v2() {
        return "media/media_video_v2";
    }
}
