package com.arc.nas.model.dto.app.media;

import com.arc.nas.model.domain.system.common.SysFile;

import java.util.List;

import static com.arc.nas.model.dto.app.media.MediaItemDTO.covertSysFileToMediaItemDTO;


public class MediaFeedDTO {

    private int cursor;         // 当前位置
    private int step;           // 每页大小
    private short hasMore;         // 是否有更多
    private long serverTime;

    private List<MediaItemDTO> content;

    public static MediaFeedDTO build(List<SysFile> sysFiles, String prefix) {
        MediaFeedDTO result = new MediaFeedDTO();
        if (sysFiles == null || sysFiles.isEmpty()) {

        } else {
            List<MediaItemDTO> mediaItemDTOS = covertSysFileToMediaItemDTO(sysFiles, prefix);
            result.setContent(mediaItemDTOS);
        }

        return result;
    }

    public Number getTotalElements() {
        return content == null ? 0 : content.size();
    }

    public int getCursor() {
        return cursor;
    }

    public void setCursor(int cursor) {
        this.cursor = cursor;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public short getHasMore() {
        return hasMore;
    }

    public void setHasMore(short hasMore) {
        this.hasMore = hasMore;
    }

    public long getServerTime() {
        return serverTime;
    }

    public void setServerTime(long serverTime) {
        this.serverTime = serverTime;
    }

    public List<MediaItemDTO> getContent() {
        return content;
    }

    public void setContent(List<MediaItemDTO> content) {
        this.content = content;
    }
}
