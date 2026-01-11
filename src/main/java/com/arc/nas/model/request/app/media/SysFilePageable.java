package com.arc.nas.model.request.app.media;

import com.arc.nas.model.constants.NormalConstants;
import com.arc.nas.model.request.ArcPageable;


public class SysFilePageable extends ArcPageable {

    private int pageNumber = NormalConstants.PAGE_NUMBER_FIRST;

    private long offset;

    private int pageSize = NormalConstants.PAGE_SIZE20;

    private String keyword;

    private String mediaType;

    // 排序字段：createTime / updateTime
    private String orderField;

    // 排序方向：asc / desc
    private String orderDirection;


    public SysFilePageable() {
    }

    public SysFilePageable(int pageNumber, int pageSize, String keyword) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.keyword = keyword;
    }

    public SysFilePageable(int pageNumber, int pageSize) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }


    @Override
    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getOrderField() {
        return orderField;
    }

    public void setOrderField(String orderField) {
        this.orderField = orderField;
    }

    public String getOrderDirection() {
        return orderDirection;
    }

    public void setOrderDirection(String orderDirection) {
        this.orderDirection = orderDirection;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }
}
