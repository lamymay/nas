package com.arc.nas.model.request.app.media;

import java.util.Set;

public class SysFileQuery {

    // 是用来搜索 名称的时候用的
    private String keyword;

    //VIDEO/IMAGE/FILE/THUMBNAIL
    private Set<String> mediaTypes;
    private Set<String> tagCodes;

    private String userId;

    // 新增：是否只显示未打标签的文件 仅限未标记
    private boolean onlyUntagged = false;

    public SysFileQuery() {
    }

    public SysFileQuery(String keyword) {
        this.keyword = keyword;
    }


    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Set<String> getMediaTypes() {
        return mediaTypes;
    }

    public void setMediaTypes(Set<String> mediaTypes) {
        this.mediaTypes = mediaTypes;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Set<String> getTagCodes() {
        return tagCodes;
    }

    public void setTagCodes(Set<String> tagCodes) {
        this.tagCodes = tagCodes;
    }

    public boolean isOnlyUntagged() {
        return onlyUntagged;
    }

    public void setOnlyUntagged(boolean onlyUntagged) {
        this.onlyUntagged = onlyUntagged;
    }
}
