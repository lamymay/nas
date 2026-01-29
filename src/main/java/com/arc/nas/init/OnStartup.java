package com.arc.nas.init;

import com.arc.nas.service.app.media.MediaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;


/**
 * 初始化操作
 */
@Component
public class OnStartup implements ApplicationListener<ContextRefreshedEvent> {
    private static final Logger log = LoggerFactory.getLogger(OnStartup.class);

    private final MediaService mediaService;

    public OnStartup(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        System.out.println("容器刷新事件 OnStartup will do mediaService.cleanThumbnails");
        try {
            mediaService.cleanThumbnails(true);
        } catch (Exception exception) {
            log.error("ERROR onApplicationEvent", exception);
        }

}
}