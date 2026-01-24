package com.arc.nas.model.request.app.media;

public class GenerateThumbnailConfig {

    /**
     * 缩略图输出目录，例如：/data/media/.thumbnails.bundle
     */
    // todo 采用配置到db的方式来做
    private String thumbnailRoot;
    private boolean force=false;

    /**
     * 缩略图宽度，320 480 ...
     */
    private int width = 480;

    /**
     * 视频抽帧时间点，默认 5 秒
     */
    private int videoFrameSecond = 5;

    /**
     * 是否覆盖已有缩略图
     */
    private boolean overwrite = false;

    /**
     *
     */
    private boolean enableUseShareIndex = false;

    /**
     * 以字节为单位 = 5MB
     */
    private Long imageSkipSize = (long) 5 * 1024 * 1024;

    public String getThumbnailRoot() {
        return thumbnailRoot;
    }

    public void setThumbnailRoot(String thumbnailRoot) {
        this.thumbnailRoot = thumbnailRoot;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getVideoFrameSecond() {
        return videoFrameSecond;
    }

    public void setVideoFrameSecond(int videoFrameSecond) {
        this.videoFrameSecond = videoFrameSecond;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public Long getImageSkipSize() {
        return imageSkipSize;
    }

    public void setImageSkipSize(Long imageSkipSize) {
        this.imageSkipSize = imageSkipSize;
    }

    public boolean isEnableUseShareIndex() {
        return enableUseShareIndex;
    }

    public void setEnableUseShareIndex(boolean enableUseShareIndex) {
        this.enableUseShareIndex = enableUseShareIndex;
    }
}
