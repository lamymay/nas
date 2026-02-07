package com.arc.nas.model.dto.app.media;

import cn.hutool.core.bean.BeanUtil;
import com.arc.nas.model.domain.system.common.SysFile;

import java.util.*;

public class MediaItemDTO {
    private String code;            // 文件编号
    private String displayName;     // 显示名称
    private String mediaType;       // VIDEO / IMAGE / AUDIO
    private String mimeType;        // MIME类型 video/mp4 / image/jpeg
    private Set<String> thumbnails;    // 缩略图URL，如果是视频可生成封面图
    private Long length;            // 文件大小，单位byte
    private String status;      // 文件状态

    private Map<String, List<MediaSegmentDTO>> segmentMap;// 不通过分辨率的视频视频切片 original 4K 720 480 240

    public static List<MediaItemDTO> covertSysFileToMediaItemDTO(List<SysFile> contents, String urlPrefix) {
        return contents.stream().map(sysFile -> {
            MediaItemDTO dto = new MediaItemDTO();
            BeanUtil.copyProperties(sysFile, dto);
            // 构建访问 URL
            String itemUrl = buildFileUrl(urlPrefix, sysFile.getCode());
            Map<String, List<MediaSegmentDTO>> segmentMap = new HashMap<String, List<MediaSegmentDTO>>();
            ArrayList<MediaSegmentDTO> mediaSegmentDTOS = new ArrayList<>();
            mediaSegmentDTOS.add(new MediaSegmentDTO(itemUrl, sysFile.getLength(), sysFile.getDuration()));
            segmentMap.put("original", mediaSegmentDTOS);
            dto.setSegmentMap(segmentMap);
            dto.setThumbnails(buildThumbnails(urlPrefix, sysFile.getThumbnail()));
            return dto;
        }).toList();
    }

    private static Set<String> buildThumbnails(String prefix, String thumbnail) {
        HashSet<String> thumbnails = new HashSet<>();
        if (thumbnail != null) {
            for (String thumbnailCode : thumbnail.split(",")) {
                thumbnails.add(buildFileUrl(prefix, thumbnailCode));
            }
        }
        return thumbnails;
    }

    // 构建文件访问 URL
    private static String buildFileUrl(String prefix, String code) {
        // 如果存储在 LOCAL，可以用 contextPath + uri
        // 如果 OSS/NAS 可以构建完整 URL
        return prefix.replace("CODE", code);
    }

    public Map<String, List<MediaSegmentDTO>> getSegmentMap() {
        return segmentMap;
    }

    public void setSegmentMap(Map<String, List<MediaSegmentDTO>> segmentMap) {
        this.segmentMap = segmentMap;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Set<String> getThumbnails() {
        return thumbnails;
    }

    public void setThumbnails(Set<String> thumbnails) {
        this.thumbnails = thumbnails;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
