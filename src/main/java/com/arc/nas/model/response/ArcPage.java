package com.arc.nas.model.response;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分页数据对象
 */
public class ArcPage<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private long total;

    private int pageSize;

    private long totalPages;

    /**
     * 列表数据
     */
    private List<T> records;

    public ArcPage() {
    }

    /**
     * 分页
     *
     * @param list  列表数据
     * @param total 总记录数
     */
    public ArcPage(List<T> list, long total) {
        this.records = list;
        this.total = total;
    }

    public static <T> ArcPage<T> build(IPage<T> page) {
        ArcPage<T> rspData = new ArcPage<>();
        rspData.setRecords(page.getRecords());
        rspData.setTotal(page.getTotal());
        return rspData;
    }

//    public static <T> ArcPage<T> build(List<T> list,long total) {
//        ArcPage<T> rspData = new ArcPage<>();
//        rspData.setRecords(list);
//        rspData.setTotal(total);
//        return rspData;
//    }

    public static <T> ArcPage<T> build() {
        return new ArcPage<>();
    }

    ///
    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public int getTotalPages() {
        return (int)this.total/((this.pageSize==0)?1:this.pageSize);
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
