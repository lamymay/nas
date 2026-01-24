package com.arc.nas.init;

import com.arc.nas.service.app.media.MediaService;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;


/**
 * 初始化操作
 */
@Component
public class OnStartup implements ApplicationListener<ContextRefreshedEvent> {

    private final MediaService mediaService;

    public OnStartup(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        System.out.println("容器刷新事件 OnStartup will do mediaService.cleanThumbnails");
        mediaService.cleanThumbnails(true);


}
}