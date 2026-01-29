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
import org.springframework.util.StopWatch;

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
    long taskEnd = System.currentTimeMillis();

    public ScheduleTask(MediaResource mediaResource,
                        SysFileDAO sysFileDAO,
                        MediaService mediaService) {
        this.mediaResource = mediaResource;
        this.sysFileDAO = sysFileDAO;
        this.mediaService = mediaService;
    }

    // 方式 A：使用注解（最简单） fixedDelay: 上一次任务结束到下一次任务开始的间隔
    @Scheduled(fixedDelay = 20000)
//    @Scheduled(initialDelay = 5000, fixedRate = 60000)
    public void scanFolder() {
        log.info("scanFolder 定时任务执行：扫描文件夹...距离上次执行完毕时间间隔{}", StringTool.getTimeStringSoFar(taskEnd));
        StopWatch stopWatch = new StopWatch("ScheduleTask");

        // 1 monitor Folders
        stopWatch.start("prepareMonitorFolders");
        Set<String> scanFolders = prepareMonitorFolders();
        stopWatch.stop();

        // 2 scan & index
        stopWatch.start("scan & index");
        mediaService.scan(scanFolders.toArray(new String[0]));
        stopWatch.stop();

        // 3 hash
        stopWatch.start("hash");
        mediaService.updateHash(false);
        stopWatch.stop();

        // 4 generateThumbnails (by index db)
        stopWatch.start("generateThumbnails");
        try {
            GenerateThumbnailResult generateThumbnailResult = mediaService.generateThumbnails(new GenerateThumbnailConfig());
            log.info("GenerateThumbnailResult={} \n ", JSON.toJSONString(generateThumbnailResult));
        } catch (Exception exception) {
            log.error("GenerateThumbnailResult 处理缩略图异常!（建议检查环境ffmpeg是否配置ok）", exception);
        }
        stopWatch.stop();
        // 替换 log.info("耗时统\n{}", stopWatch.prettyPrint());
        log.info("任务耗时统计 (ms):\n{}", formatStopWatchToMs(stopWatch));

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





    // 辅助方法
    private String formatStopWatchToMs(StopWatch sw) {
        StringBuilder sb = new StringBuilder();
        sb.append("---------------------------------------------\n");
        sb.append("ms          %     Task name\n");
        sb.append("---------------------------------------------\n");
        for (StopWatch.TaskInfo task : sw.getTaskInfo()) {
            // 将纳秒转换为毫秒，保留 3 位小数或直接取整
            double ms = task.getTimeNanos() / 1_000_000.0;
            double percent = (double) task.getTimeNanos() / sw.getTotalTimeNanos();

            sb.append(String.format("%-10.2f  %03d%%  %s%n",
                    ms,
                    Math.round(percent * 100),
                    task.getTaskName()));
        }
        sb.append("---------------------------------------------\n");
        sb.append(String.format("Total: %.2f ms", sw.getTotalTimeMillis() * 1.0));
        return sb.toString();
    }
}