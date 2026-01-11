package com.arc.nas.timer;

import com.arc.nas.model.domain.app.media.MediaFileResource;
import com.arc.nas.model.request.app.media.GenerateThumbnailConfig;
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
        log.info("scanFolder 定时任务执行：整体耗时:{}\nfolders={}", StringTool.getTimeStringSoFar(scanFolderT0),
                JSON.toJSONString(scanFolders));

        // 2 scan folder is file --index db
        mediaService.scan(scanFolders.toArray(new String[0]));

        // 3 hash
        mediaService.updateHash(false);

        // 4 generateThumbnails (by index db)

        try {
            GenerateThumbnailConfig config = new GenerateThumbnailConfig();
            config.setForce(false);
            config.setOverwrite(true);
            mediaService.generateThumbnails(config);
        } catch (Exception exception) {
            log.error("处理缩略图异常!（建议检查环境ffmpeg是否配置ok）", exception);
        }

        latest = System.currentTimeMillis();
    }

    private Set<String> prepareMonitorFolders() {

        List<MediaFileResource> mediaFileResources = mediaResource.listAll();
        // 你的业务逻辑，如：watcherManager.upsertWatcher(...)
        Set<String> folders = new HashSet<>();
        for (MediaFileResource mediaFileResource : mediaFileResources) {
            if (mediaFileResource != null && mediaFileResource.getPath() != null) {
                folders.add(mediaFileResource.getPath());
            }
        }
//        long afterMediaResourceListAll = System.currentTimeMillis();
//        log.info("scanFolder  耗时统计 mediaResource.listAll {}", StringTool.displayTimeWithUnit(scanFolderT0, afterMediaResourceListAll));
//
//        List<SysFile> sysFiles = sysFileDAO.listAll();
//        long afterFileDAOListAll = System.currentTimeMillis();
//        log.info("scanFolder  耗时统计 sysFileDAO.listAll {}", StringTool.displayTimeWithUnit(afterFileDAOListAll, afterMediaResourceListAll));

//        Set<String> parentFolder = new HashSet<>();
//        if (sysFiles != null && !sysFiles.isEmpty()) {
//            for (SysFile sysFile : sysFiles) {
//                if (sysFile == null) continue;
//                if (MediaServiceImpl.nomedia.contains(sysFile.getOriginalName())
//                ||ignore.contains(sysFile.getOriginalName())) {
//                    continue;
//                }
//                File file = new File(sysFile.getPath());
//                if (file.isFile()) {
//                    parentFolder.add(file.getParent());
//                } else {
//                    parentFolder.add(file.getParent());
//                }
//            }
//        }
//        log.info("scanFolder  耗时统计 sysFileDAO.listAll 推导父级路径 {}", StringTool.displayTimeWithUnit(afterFileDAOListAll, System.currentTimeMillis()));
//
//        if (parentFolder.size() > 0) {
//            folders.addAll(parentFolder);
//        }
        return folders;
    }

    // 方式 B：手动通过线程池提交（最灵活，适合动态调整频率）
//    @Autowired
//    private ThreadPoolTaskScheduler taskScheduler;
//    public void startDynamicTask() {
//        taskScheduler.scheduleAtFixedRate(() -> {
//            log.info("手动提交的任务正在运行...");
//        }, 10000); // 每10秒运行一次
//    }
}