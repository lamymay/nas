package com.arc.nas.model.request.system.file;

import java.io.Serializable;
import java.util.Date;

/**
 * 系统文件记录请求
 */
public class SysFileRequest implements Serializable {

    private Long id;//自增主键
    private String name;// 显示名称
    private String suffix;// 后缀

    private Integer version;// 版本信息id
    private String key;// 文件标识
    private String uri;// 文件存放uri

    private String checkType;// 校验方式
    private String checkCode;// 校验码
    private Integer state;// 逻辑删除用的标识

    private Long size;// 文件大小
    private String sizeUnit;// 文件大小单位
    private String remark;// 描述

    private String code;// 版本号
    private String type;// 类型

    private Date createTime;// 创建时间
    private Date updateTime;// 更新时间
    private Date createAt;// 创建时间
    private Date updateAt;// 更新时间
    private int currentPage = DefaultPageParameter.DEFAULT_CURRENT_PAGE;
    private int pageSize = DefaultPageParameter.DEFAULT_PAGE_SIZE;

    public SysFileRequest() {
    }


    public SysFileRequest(Long id) {
        this.id = id;
    }


    public SysFileRequest(String code) {
        this.code = code;
    }

    /**
     * @return db分页查询开始行
     */
    public int getStart() {
        return (getCurrentPage() - 1) * getPageSize();
    }

    /**
     * @return db分页查询结束行
     */
    public int getEnd() {
        return getCurrentPage() * getPageSize();
    }

    public int getPageSize() {
        return pageSize < 1 ? DefaultPageParameter.DEFAULT_PAGE_SIZE : pageSize;
    }

    /**
     * pageSize set时候保证有合法值
     *
     * @param pageSize 页面大小
     */
    public void setPageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            this.pageSize = DefaultPageParameter.DEFAULT_PAGE_SIZE;
        }
        this.pageSize = pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCurrentPage() {
        return currentPage < 1 ? DefaultPageParameter.DEFAULT_CURRENT_PAGE : currentPage;
    }

    /**
     * currentPage set当前页保证有合法值, 当前页从前端显示第一页开始
     *
     * @param currentPage 当前页
     */
    public void setCurrentPage(Integer currentPage) {
        if (currentPage == null || currentPage < 1) {
            this.currentPage = DefaultPageParameter.DEFAULT_CURRENT_PAGE;
        }
        this.currentPage = currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getCheckType() {
        return checkType;
    }

    public void setCheckType(String checkType) {
        this.checkType = checkType;
    }

    public String getCheckCode() {
        return checkCode;
    }

    public void setCheckCode(String checkCode) {
        this.checkCode = checkCode;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getSizeUnit() {
        return sizeUnit;
    }

    public void setSizeUnit(String sizeUnit) {
        this.sizeUnit = sizeUnit;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public Date getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Date updateAt) {
        this.updateAt = updateAt;
    }

    interface DefaultPageParameter {
        /**
         * 默认第一页是1
         */
        int DEFAULT_CURRENT_PAGE = 1;

        /**
         * 默认分页大小查20个
         */
        int DEFAULT_PAGE_SIZE = 20;
    }
}
