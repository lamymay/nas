package com.arc.nas.model.request.app.media;

public class CleanThumbnailsResult {

    private boolean success;
    private String message;
    private int total;
    private int deleteCount;

    public CleanThumbnailsResult(boolean success) {
        this.success = success;
    }

    public static CleanThumbnailsResult ok() {
        return new CleanThumbnailsResult(true);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getDeleteCount() {
        return deleteCount;
    }

    public void setDeleteCount(int deleteCount) {
        this.deleteCount = deleteCount;
    }
}
