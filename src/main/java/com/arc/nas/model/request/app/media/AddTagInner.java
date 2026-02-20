package com.arc.nas.model.request.app.media;

import java.util.List;

public class AddTagInner {

    private Long tagId;

    private List<Long> fileIds;

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }

    public List<Long> getFileIds() {
        return fileIds;
    }

    public void setFileIds(List<Long> fileIds) {
        this.fileIds = fileIds;
    }
}
