package com.arc.nas.model.request.app.media;

import java.util.List;

public class RemoveTagBatchRequest {

    private List<RemoveTagRequest> removeTagRequests;

    public List<RemoveTagRequest> getRemoveTagRequests() {
        return removeTagRequests;
    }

    public void setRemoveTagRequests(List<RemoveTagRequest> removeTagRequests) {
        this.removeTagRequests = removeTagRequests;
    }
}
