package com.arc.nas.model.request.app.media;

import java.util.List;

public class BatchResult {

    private boolean success;
    private String message;
    private List<BatchItemResult> content;

    public BatchResult() {
    }

    public BatchResult(boolean success) {
        this.success = success;
    }

    public BatchResult(boolean success, String message) {
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

    public List<BatchItemResult> getContent() {
        return content;
    }

    public void setContent(List<BatchItemResult> content) {
        this.content = content;
    }
}
