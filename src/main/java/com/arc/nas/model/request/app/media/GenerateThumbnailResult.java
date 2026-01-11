package com.arc.nas.model.request.app.media;

import java.util.Map;

public class GenerateThumbnailResult {
    private String status;
    private String message;
    private Map<String, Object> extendMap;
    private GenerateThumbnailItemResult imageResult;
    private GenerateThumbnailItemResult videoResult;

    private int total;

    private int updateCount;

    private int errorCount;

    public GenerateThumbnailResult() {
    }

    public GenerateThumbnailResult(GenerateThumbnailItemResult imageResult, GenerateThumbnailItemResult videoResult) {
        this.imageResult = imageResult;
        this.videoResult = videoResult;

        int totalFinal = 0;
        int updateCountFinal = 0;
        int errorCountFinal = 0;
        if (imageResult != null) {
            totalFinal = totalFinal + imageResult.getTotal();
            updateCountFinal = updateCountFinal + imageResult.getUpdateCount();
            errorCountFinal = errorCountFinal + imageResult.getErrorCount();

        }
        if (videoResult != null) {
            totalFinal = totalFinal + videoResult.getTotal();
            updateCountFinal = updateCountFinal + videoResult.getUpdateCount();
            errorCountFinal = errorCountFinal + videoResult.getErrorCount();
        }

        this.total = totalFinal;
        this.updateCount = updateCountFinal;
        this.errorCount = errorCountFinal;

    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getUpdateCount() {
        return updateCount;
    }

    public void setUpdateCount(int updateCount) {
        this.updateCount = updateCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public GenerateThumbnailItemResult getImageResult() {
        return imageResult;
    }

    public void setImageResult(GenerateThumbnailItemResult imageResult) {
        this.imageResult = imageResult;
    }

    public GenerateThumbnailItemResult getVideoResult() {
        return videoResult;
    }

    public void setVideoResult(GenerateThumbnailItemResult videoResult) {
        this.videoResult = videoResult;
    }

    public Map<String, Object> getExtendMap() {
        return extendMap;
    }

    public void setExtendMap(Map<String, Object> extendMap) {
        this.extendMap = extendMap;
    }
}
