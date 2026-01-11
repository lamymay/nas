package com.arc.nas.model.dto.app.media;

import java.util.List;


public class MediaPageDTO {

    private List<MediaItemDTO> content; // 当前页数据

    private Integer pageNumber;         // 当前页码
    private Integer pageSize;           // 每页大小
    private Long totalElements;         // 总记录数
    private Integer totalPages;         // 总页数


    public List<MediaItemDTO> getContent() {
        return content;
    }

    public void setContent(List<MediaItemDTO> content) {
        this.content = content;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
}
