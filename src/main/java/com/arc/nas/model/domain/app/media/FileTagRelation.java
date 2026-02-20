package com.arc.nas.model.domain.app.media;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

// tag到媒体文件的关系表
@TableName("media_file_tag_relation")
public class FileTagRelation {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long tagId;
    private Long fileId;

    public FileTagRelation() {
    }

    public FileTagRelation(Long id, Long fileId, Long tagId) {
        this.id = id;
        this.fileId = fileId;
        this.tagId = tagId;
    }

    public FileTagRelation(Long fileId, Long tagId) {
        this.fileId = fileId;
        this.tagId = tagId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }
}
