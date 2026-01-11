package com.arc.nas.model.domain.app.media;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;


/**
 * 标签
 */
@TableName("media_tag")
public class MediaTag implements Serializable {

    @TableId(value = "code", type = IdType.ASSIGN_UUID)
    private String code;//编号
    private Date createTime;// 创建时间
    private Date updateTime;// 更新时间
    private String displayName;// 显示名称（可为空，默认=originalName）
    private String avatar;// 多个缩略图 用英文逗号分割
    private String remark;// 描述


    public MediaTag() {

    }


    public MediaTag(String path, String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}

