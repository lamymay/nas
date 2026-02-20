package com.arc.nas.model.request.app.media;

import java.util.Set;

public class SysFileQuery {

    // 是用来搜索 名称的时候用的
    private String keyword;

    //VIDEO/IMAGE/FILE/THUMBNAIL
    private Set<String> mediaTypes;

    // 具体特定的文件格式
    private Set<String> mimeTypes;

    private Set<Long> tagIds;

//    @Deprecated private String userId;

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

    public Set<Long> getTagIds() {
        return tagIds;
    }

    public void setTagIds(Set<Long> tagIds) {
        this.tagIds = tagIds;
    }

    public boolean isOnlyUntagged() {
        return onlyUntagged;
    }

    public void setOnlyUntagged(boolean onlyUntagged) {
        this.onlyUntagged = onlyUntagged;
    }

    public Set<String> getMimeTypes() {
        return mimeTypes;
    }

    public void setMimeTypes(Set<String> mimeTypes) {
        this.mimeTypes = mimeTypes;
    }
}
