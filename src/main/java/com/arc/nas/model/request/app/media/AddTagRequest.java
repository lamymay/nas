package com.arc.nas.model.request.app.media;

import java.util.List;

public class AddTagRequest {

    private String tagCode;

    private List<String> fileCodes;

    public String getTagCode() {
        return tagCode;
    }

    public void setTagCode(String tagCode) {
        this.tagCode = tagCode;
    }

    public List<String> getFileCodes() {
        return fileCodes;
    }

    public void setFileCodes(List<String> fileCodes) {
        this.fileCodes = fileCodes;
    }
}
