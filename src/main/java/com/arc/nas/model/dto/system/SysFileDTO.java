package com.arc.nas.model.dto.system;

import java.util.Date;

/**
 * @author may
 * @since 2021.09.27 6:36 下午
 */

public class SysFileDTO {

    private Long id;//id
    private Date createTime;// 创建时间
    private Date updateTime;// 更新时间

    private String code;//编号

    //文件 本身属性： 名称/大小/后缀/文件位置/类型
    private String name;// 显示名称
    private String lengthUnit;// 文件大小单位
    private Long length;// 文件大小

    private String suffix;// 后缀
    private String type;// 类型 文件还是图片  一般来说图片是可以直接预览的,

    private String remark;// 描述

    private String path;// 文件本地存放位置--如果是自己的环境应该是服务器的绝对路径

    private String location;// 文件持久化位置，用于 判断拼接url前部分    SERVER / DEVELOP
    private String host;// 文件持久化位置，用于 判断拼接url前部分    SERVER / DEVELOP  或者偷懒直接写 ip
    private String uri;// 文件存放 url=      {host}:{port} +/xxx/yyy/zzz

    private String thumbnailUri;//缩略图 注意仅仅图片应该由此相数据 图片类型有此属性，缩略图地址，其磁盘路径与主图path相似，在文件名称中加入small标识，格式：123_small.png

    private Integer version;// 版本信息id
    private Integer state;// 逻辑删除用的标识

    private String key;// 文件标识
    private String checkType;// 校验方式
    private String checkCode;// 校验码

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLengthUnit() {
        return lengthUnit;
    }

    public void setLengthUnit(String lengthUnit) {
        this.lengthUnit = lengthUnit;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getThumbnailUri() {
        return thumbnailUri;
    }

    public void setThumbnailUri(String thumbnailUri) {
        this.thumbnailUri = thumbnailUri;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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
}
