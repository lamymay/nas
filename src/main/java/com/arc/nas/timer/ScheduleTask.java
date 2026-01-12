package com.arc.nas.timer;

import com.arc.nas.model.domain.app.media.MediaFileResource;
import com.arc.nas.model.request.app.media.GenerateThumbnailConfig;
import com.arc.nas.model.request.app.media.GenerateThumbnailResult;
import com.arc.nas.service.app.media.MediaResource;
import com.arc.nas.service.app.media.MediaService;
import com.arc.nas.service.system.common.SysFileDAO;
import com.arc.util.JSON;
import com.arc.util.StringTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.arc.util.StringTool.displayTimeWithUnit;

@Component
public class ScheduleTask {

    private static final Logger log = LoggerFactory.getLogger(ScheduleTask.class);
    private final MediaResource mediaResource;
    private final SysFileDAO sysFileDAO;
    private final MediaService mediaService;
    //SysFileFolder
    long latest = System.currentTimeMillis();

    public ScheduleTask(MediaResource mediaResource,
                        SysFileDAO sysFileDAO,
                        MediaService mediaService) {
        this.mediaResource = mediaResource;
        this.sysFileDAO = sysFileDAO;
        this.mediaService = mediaService;
    }

    // 方式 A：使用注解（最简单） fixedDelay: 上一次任务结束到下一次任务开始的间隔
    @Scheduled(fixedDelay = 2000)
    public void scanFolder() {
        long scanFolderT0 = System.currentTimeMillis();
        log.info("scanFolder 定时任务执行：扫描文件夹...距离上次执行完毕时间间隔{}", StringTool.getTimeStringSoFar(latest));

        // 1 monitor Folders
        Set<String> scanFolders = prepareMonitorFolders();

        // 2 scan folder is file --index db
        mediaService.scan(scanFolders.toArray(new String[0]));

        // 3 hash
        long updateHashStart = System.currentTimeMillis();
        mediaService.updateHash(false);
        long updateHashEnd = System.currentTimeMillis();

        // 4 generateThumbnails (by index db)
        long generateThumbnailResultT0 = System.currentTimeMillis();
        GenerateThumbnailResult generateThumbnailResult = null;
        try {
            GenerateThumbnailConfig config = new GenerateThumbnailConfig();
            config.setForce(false);
            config.setOverwrite(true);
            generateThumbnailResult = mediaService.generateThumbnails(config);
        } catch (Exception exception) {
            log.error("处理缩略图异常!（建议检查环境ffmpeg是否配置ok）", exception);
        }
        long generateThumbnailResultTimeEnd = System.currentTimeMillis();
        latest = System.currentTimeMillis();

        log.info("scanFolder result={} \n 耗时统 整体耗时={} hash处理={}处理缩略图={}",
                JSON.toJSONString(generateThumbnailResult),
                displayTimeWithUnit(updateHashStart, updateHashEnd),
                displayTimeWithUnit(scanFolderT0, generateThumbnailResultTimeEnd),
                displayTimeWithUnit(generateThumbnailResultT0, generateThumbnailResultTimeEnd));

    }

    private Set<String> prepareMonitorFolders() {
        List<MediaFileResource> mediaFileResources = mediaResource.listAll();
        Set<String> folders = new HashSet<>();
        for (MediaFileResource mediaFileResource : mediaFileResources) {
            if (mediaFileResource != null && mediaFileResource.getPath() != null) {
                folders.add(mediaFileResource.getPath());
            }
        }
        return folders;
    }


}