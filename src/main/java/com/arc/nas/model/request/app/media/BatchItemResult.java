package com.arc.nas.model.request.app.media;

public class BatchItemResult {

    private boolean success;
    private String message;

    public BatchItemResult() {
    }

    public BatchItemResult(boolean success) {
        this.success = success;
    }

    public BatchItemResult(boolean success, String message) {
        this.success = success;
        this.message = message;
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
}
