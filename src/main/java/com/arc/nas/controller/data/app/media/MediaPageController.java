package com.arc.nas.controller.data.app.media;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MediaPageController {

    private static final Logger log = LoggerFactory.getLogger(MediaPageController.class);

    @GetMapping("/media/home")
    public String homePage(Model model) {
        return "/media/media_home";
    }

    @GetMapping("/media/video")
    public String homeVideo(Model model) {
        return "/media/media_video";
    }

    @GetMapping("/media/image")
    public String videoImage(Model model) {
        return "/media/media_image";
    }

    @GetMapping("/media/cms")
    public String videoCMS(Model model) {
        // 页面初始加载时不需要传数据，由前端通过接口动态请求
        return "/media/media_cms";
    }


}
