package com.arc.nas.model.request.app.media;

import java.util.List;

public class RemoveTagRequest {

    private String fileCode;

    private List<String> tagCodes;

    public String getFileCode() {
        return fileCode;
    }

    public void setFileCode(String fileCode) {
        this.fileCode = fileCode;
    }

    public List<String> getTagCodes() {
        return tagCodes;
    }

    public void setTagCodes(List<String> tagCodes) {
        this.tagCodes = tagCodes;
    }
}
