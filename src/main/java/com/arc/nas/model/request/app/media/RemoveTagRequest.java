package com.arc.nas.model.request.app.media;

import java.util.List;

public class RemoveTagRequest {

    private Long fileId;

    private List<Long> tagIds;

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public List<Long> getTagIds() {
        return tagIds;
    }

    public void setTagIds(List<Long> tagIds) {
        this.tagIds = tagIds;
    }
}
